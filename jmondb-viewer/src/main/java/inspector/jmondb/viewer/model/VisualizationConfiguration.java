package inspector.jmondb.viewer.model;

import com.google.common.collect.ImmutableMap;
import inspector.jmondb.model.EventType;

import java.awt.*;
import java.util.Map;

public class VisualizationConfiguration extends Configuration {

    private final Map<String, Integer> colorDefaults = ImmutableMap.of(
            "color.undefined", Color.ORANGE.getRGB(),
            "color.calibration", Color.GREEN.getRGB(),
            "color.maintenance", Color.BLUE.getRGB(),
            "color.incident", Color.RED.getRGB()
    );

    public VisualizationConfiguration() {
        super();
    }

    public Color getColor(EventType type) {
        String key = type != null ? "color." + type.toString() : "unknown";
        int def = colorDefaults.containsKey(key) ? colorDefaults.get(key) : Color.BLACK.getRGB();
        return new Color(PREFERENCES.getInt(key, def));
    }
}
