package inspector.imondb.collector.model.config;

import java.util.Objects;

public class DeviceInfo {

    private String name;
    private String temperatureUid;
    private boolean temperatureActive;
    private String motionUid;
    private boolean motionActive;

    public DeviceInfo(String name, String temperatureUid, boolean temperatureActive, String motionUid, boolean motionActive) {
        this.name = name;
        this.temperatureUid = temperatureUid;
        this.temperatureActive = temperatureActive;
        this.motionUid = motionUid;
        this.motionActive = motionActive;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTemperatureUid() {
        return temperatureUid;
    }

    public void setTemperatureUid(String temperatureUid) {
        this.temperatureUid = temperatureUid;
    }

    public boolean getTemperatureActive() {
        return temperatureActive;
    }

    public void setTemperatureActive(boolean isActive) {
        temperatureActive = isActive;
    }

    public String getMotionUid() {
        return motionUid;
    }

    public void setMotionUid(String motionUid) {
        this.motionUid = motionUid;
    }
    public boolean getMotionActive() {
        return motionActive;
    }

    public void setMotionActive(boolean isActive) {
        motionActive = isActive;
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        DeviceInfo that = (DeviceInfo) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(temperatureUid, that.temperatureUid) &&
                Objects.equals(motionUid, that.motionUid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, temperatureUid, motionUid);
    }
}
