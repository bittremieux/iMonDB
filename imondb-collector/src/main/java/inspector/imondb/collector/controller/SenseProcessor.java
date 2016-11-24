package inspector.imondb.collector.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import inspector.imondb.collector.model.config.DeviceInfo;
import inspector.imondb.io.IMonDBReader;
import inspector.imondb.io.IMonDBWriter;
import inspector.imondb.model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SenseProcessor implements Runnable {

    private static final Logger LOGGER = LogManager.getLogger(SenseProcessor.class);

    private String BASE_URL = "https://apis.sen.se/v2/";

    private final IMonDBReader dbReader;
    private final IMonDBWriter dbWriter;
    private String username;
    private String password;
    private DeviceInfo deviceInfo;
    private Timestamp startDate;
    private int timeOutMilliseconds;

    private SimpleDateFormat DATE_PARSER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    public SenseProcessor(IMonDBReader dbReader, IMonDBWriter dbWriter, String username, String password,
                          DeviceInfo deviceInfo, Timestamp startDate, int timeOutMilliseconds) {
        this.dbReader = dbReader;
        this.dbWriter = dbWriter;
        this.deviceInfo = deviceInfo;
        this.username = username;
        this.password = password;
        this.startDate = startDate;
        this.timeOutMilliseconds = timeOutMilliseconds;
    }

    public SenseProcessor(IMonDBReader dbReader, IMonDBWriter dbWriter, String username, String password,
                          DeviceInfo deviceInfo) {
        this(dbReader, dbWriter, username, password, deviceInfo, null, 0);
    }

    @Override
    public void run() {
        try {
            LOGGER.info("Process external device <{}>", deviceInfo.getName());

            Client client = ClientBuilder.newClient();

            // retrieve the Sen.se API key from the username and password
            Form apiKeyForm = new Form();
            apiKeyForm.param("username", username);
            apiKeyForm.param("password", password);
            String apiKeyJson = client.target(BASE_URL).path("/user/api_key/")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .post(Entity.entity(apiKeyForm, MediaType.APPLICATION_FORM_URLENCODED_TYPE), String.class);
            ObjectMapper mapper = new ObjectMapper();
            Map<String, String> apiKeyMap = mapper.readValue(apiKeyJson, new TypeReference<Map<String, String>>() {});
            String apiKey = "Token " + apiKeyMap.get("token");

            // read the device's temperature values (if applicable)
            if(deviceInfo.getTemperatureActive()) {
                List<Event> events = readDevice(client, apiKey, deviceInfo.getTemperatureUid(), true);
                synchronized(dbWriter) {
                    events.forEach(dbWriter::writeOrUpdateEvent);
                }
            }
            // read the device's motion values (if applicable)
            if(deviceInfo.getMotionActive()) {
                List<Event> events = readDevice(client, apiKey, deviceInfo.getMotionUid(), false);
                synchronized(dbWriter) {
                    events.forEach(dbWriter::writeOrUpdateEvent);
                }
            }
        } catch(IOException | ParseException | InterruptedException e) {
            LOGGER.error("Error while retrieving external data: {}", e);
            throw new IllegalStateException("Error while retrieving external data: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private List<Event> readDevice(Client client, String apiKey, String uid, boolean isTemperature)
            throws IOException, ParseException, InterruptedException {
        Instrument instrument = dbReader.getInstrument(deviceInfo.getName());

        List<Event> events = new ArrayList<>();

        WebTarget target = client.target(BASE_URL).path("/feeds/" + uid + "/events/");
        if(startDate != null) {
            target = target.queryParam("gt", DATE_PARSER.format(startDate));
        }

        while(target != null) {
            Response eventsResponse = target
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .header(HttpHeaders.AUTHORIZATION, apiKey)
                    .get();
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> eventsMap = mapper.readValue(eventsResponse.readEntity(String.class),
                    new TypeReference<Map<String, Object>>() {});

            for(Map<String, Object> object : ((List<Map<String, Object>>) eventsMap.get("objects"))) {
                Timestamp date = new Timestamp(DATE_PARSER.parse((String) object.get("dateEvent")).getTime());
                Event event;
                if(isTemperature) {
                    double degreeCelsius = ((Map<String, Integer>) object.get("data")).get("centidegreeCelsius") / 100.0;
                    event = new Event(instrument, date, EventType.TEMPERATURE, null, null, Double.toString(degreeCelsius));
                } else {
                    event = new Event(instrument, date, EventType.MOTION);
                }
                events.add(event);
            }

            String next = ((Map<String, String>) eventsMap.get("links")).get("next");
            if(next != null) {
                target = client.target(next);
                Thread.sleep(timeOutMilliseconds);
            } else {
                target = null;
            }
        }

        return events;
    }
}
