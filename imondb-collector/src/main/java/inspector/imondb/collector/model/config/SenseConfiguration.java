package inspector.imondb.collector.model.config;

import com.google.common.collect.ImmutableMap;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import org.jasypt.util.text.BasicTextEncryptor;

import java.util.*;
import java.util.stream.Collectors;

public class SenseConfiguration {

    private Map<String, Object> rootMap;

    private BasicTextEncryptor textEncryptor;

    public SenseConfiguration(Map<String, Object> map) {
        rootMap = map;

        textEncryptor = new BasicTextEncryptor();
        textEncryptor.setPassword("Different encryption password for the Sen.se configuration.");
    }

    public String getUserName() {
        if(rootMap.containsKey("sense")) {
            @SuppressWarnings("unchecked")
            String result = ((Map<String, String>) rootMap.get("sense")).get("user");
            return result;
        } else {
            return null;
        }
    }

    public void setUserName(String username) {
        if(!rootMap.containsKey("sense")) {
            rootMap.put("sense", new HashMap<String, Object>());
        }
        @SuppressWarnings("unchecked")
        Map<String, String> senseMap = (Map<String, String>) rootMap.get("sense");
        senseMap.put("user", username);
    }

    public String getPassword() {
        if(rootMap.containsKey("sense")) {
            @SuppressWarnings("unchecked")
            String result = ((Map<String, String>) rootMap.get("sense")).get("password");
            try {
                return textEncryptor.decrypt(result);
            } catch(EncryptionOperationNotPossibleException e) {
                return result;
            }
        } else {
            return null;
        }
    }

    public void setPassword(String password) {
        if(!rootMap.containsKey("sense")) {
            rootMap.put("sense", new HashMap<String, Object>());
        }
        @SuppressWarnings("unchecked")
        Map<String, String> senseMap = (Map<String, String>) rootMap.get("sense");
        senseMap.put("password", textEncryptor.encrypt(password));
    }

    public List<DeviceInfo> getDevices() {
        if(rootMap.containsKey("sense")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> senseMap = (Map<String, Object>) rootMap.get("sense");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> temp = (List<Map<String, Object>>) senseMap.get("devices");

            if(temp != null) {
                List<DeviceInfo> devices = new ArrayList<>(temp.size());
                devices.addAll(temp.stream().map(map -> new DeviceInfo((String) map.get("name"),
                        (String) map.get("temperature_uid"), (Boolean) map.get("temperature_active"),
                        (String) map.get("motion_uid"), (Boolean) map.get("motion_active"))).collect(Collectors.toList()));

                return devices;
            } else {
                return Collections.emptyList();
            }
        } else {
            return Collections.emptyList();
        }
    }

    public void setDevices(Collection<DeviceInfo> devices) {
        if(!rootMap.containsKey("sense")) {
            rootMap.put("sense", new HashMap<String, Object>());
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> senseMap = (Map<String, Object>) rootMap.get("sense");

        List<Map<String, Object>> devicesConfig = new ArrayList<>();
        for(DeviceInfo device : devices) {
            devicesConfig.add(ImmutableMap.of("name", device.getName(),
                    "temperature_uid", device.getTemperatureUid(), "temperature_active", device.getTemperatureActive(),
                    "motion_uid", device.getMotionUid(), "motion_active", device.getMotionActive()));
        }
        Collections.sort(devicesConfig, (o1, o2) -> ((String) o1.get("name")).compareTo((String) o2.get("name")));

        // overwrite the previous config
        senseMap.put("devices", devicesConfig);
    }
}
