package inspector.imondb.viewer.model;

/*
 * #%L
 * iMonDB Viewer
 * %%
 * Copyright (C) 2014 - 2015 InSPECtor
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.google.common.collect.ImmutableMap;
import inspector.imondb.model.EventType;

import java.awt.*;
import java.util.Map;

public class VisualizationConfiguration extends Configuration {

    private static final Map<String, Integer> COLOR_DEFAULTS = ImmutableMap.of(
            "color_undefined", Color.ORANGE.getRGB(),
            "color_calibration", Color.GREEN.getRGB(),
            "color_maintenance", Color.BLUE.getRGB(),
            "color_incident", Color.RED.getRGB()
    );

    public VisualizationConfiguration() {
        super();
    }

    public Color getColor(EventType type) {
        String key = type != null ? "color_" + type.toString() : "unknown";
        int def = COLOR_DEFAULTS.containsKey(key) ? COLOR_DEFAULTS.get(key) : Color.BLACK.getRGB();
        return new Color(preferences.getInt(key, def));
    }

    public void setColor(EventType type, Color color) {
        if(type != null) {
            String key = "color_" + type.toString();
            preferences.putInt(key, color.getRGB());
        }
    }

    public void reset() {
        for(Map.Entry<String, Integer> entry : COLOR_DEFAULTS.entrySet()) {
            preferences.putInt(entry.getKey(), entry.getValue());
        }
    }
}
