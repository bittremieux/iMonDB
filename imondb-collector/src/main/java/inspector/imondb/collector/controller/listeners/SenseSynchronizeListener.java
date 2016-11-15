package inspector.imondb.collector.controller.listeners;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import inspector.imondb.collector.model.config.DeviceInfo;
import inspector.imondb.collector.view.gui.CollectorFrame;
import inspector.imondb.collector.view.gui.external.ExternalPanel;

import javax.swing.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

public class SenseSynchronizeListener implements ActionListener {

    private final String BASE_URL = "https://apis.sen.se/v2/";

    private CollectorFrame collectorFrame;

    public SenseSynchronizeListener(CollectorFrame collectorFrame) {
        this.collectorFrame = collectorFrame;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        collectorFrame.setWaitCursor(true);

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            @SuppressWarnings("unchecked")
            protected Void doInBackground() throws Exception {
                ExternalPanel externalPanel = collectorFrame.getExternalPanel();

                Client client = ClientBuilder.newClient();
                ObjectMapper mapper = new ObjectMapper();

                // retrieve the Sen.se API key from the username and password
                Form apiKeyForm = new Form();
                apiKeyForm.param("username", externalPanel.getUserName());
                apiKeyForm.param("password", externalPanel.getPassword());
                String apiKeyJson = client.target(BASE_URL).path("/user/api_key/")
                        .request(MediaType.APPLICATION_JSON_TYPE)
                        .post(Entity.entity(apiKeyForm, MediaType.APPLICATION_FORM_URLENCODED_TYPE), String.class);
                Map<String, String> apiKeyMap = mapper.readValue(apiKeyJson, new TypeReference<Map<String, String>>() {
                });
                String apiKey = "Token " + apiKeyMap.get("token");

                // get all Cookies
                Response userResponse = client.target(BASE_URL).path("/nodes/")
                        .queryParam("resource__type", "device")
                        .queryParam("resource__slug", "cookie")
                        .request(MediaType.APPLICATION_JSON_TYPE)
                        .header(HttpHeaders.AUTHORIZATION, apiKey)
                        .get();
                Map<String, Object> nodesMap = mapper.readValue(userResponse.readEntity(String.class),
                        new TypeReference<Map<String, Object>>() {
                        });
                List<Object> objectsList = (List<Object>) nodesMap.get("objects");
                for(Object object : objectsList) {
                    Map<String, Object> objectMap = (Map<String, Object>) object;
                    String objectLabel = (String) objectMap.get("label");
                    String temperatureUid = null;
                    String motionUid = null;

                    // retrieve the temperature and motion UID's if these are active for this cookie
                    List<Object> publishesList = (List<Object>) objectMap.get("publishes");
                    for(Object publishes : publishesList) {
                        Map<String, Object> publishesMap = (Map<String, Object>) publishes;
                        String publishesLabel = (String) publishesMap.get("label");
                        if(((Boolean) publishesMap.get("used")) && "Temperature".equals(publishesLabel)) {
                            temperatureUid = (String) publishesMap.get("uid");
                        } else if(((Boolean) publishesMap.get("used")) && "Motion".equals(publishesLabel)) {
                            motionUid = (String) publishesMap.get("uid");
                        }
                    }

                    // add the cookie to the application
                    externalPanel.addCookie(new DeviceInfo(objectLabel, temperatureUid, temperatureUid!= null,
                            motionUid, motionUid != null));
                }

                externalPanel.setSynchronized(true);

                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                } catch(InterruptedException | ExecutionException ex) {
                    // synchronization failed
                    JOptionPane.showMessageDialog(Frame.getFrames()[0],
                            "<html><b>Could not synchronize the Sen.se smart monitors</b></html>\n"
                                    + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                } catch(CancellationException ignored) {
                }

                // update the configuration
                collectorFrame.getExternalPanel().notifyObservers();
                // update the overview tab
                collectorFrame.getOverviewPanel().update();

                collectorFrame.setWaitCursor(false);
            }
        };

        // synchronize the smart monitors
        worker.execute();
    }
}
