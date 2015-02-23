package inspector.imondb.collector.model.config;

/*
 * #%L
 * iMonDB Collector
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.List;
import java.util.Map;

public class Configuration {

    private static final Logger LOGGER = LogManager.getLogger(Configuration.class);

    /** {@link Yaml} object to load and write YAML files */
    private Yaml yaml;

    /** the YAML config file represented as nested {@link Map}s and {@link List}s */
    private Map<String, Object> rootMap;

    private DatabaseConfiguration databaseConfiguration;
    private GeneralConfiguration generalConfiguration;
    private InstrumentConfiguration instrumentConfiguration;
    private MetadataConfiguration metadataConfiguration;

    public Configuration(File file) {
        try {
            InputStream inputStream;
            if(file != null && file.exists()) {
                // load the specified config file
                inputStream = new FileInputStream(file);
            } else {
                // else load the standard config file
                inputStream = getClass().getResourceAsStream("/config.yaml");
            }

            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            options.setIndent(4);

            yaml = new Yaml(options);

            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) yaml.load(inputStream);
            inputStream.close();

            rootMap = map;
            databaseConfiguration = new DatabaseConfiguration(rootMap);
            generalConfiguration = new GeneralConfiguration(rootMap);
            instrumentConfiguration = new InstrumentConfiguration(rootMap);
            metadataConfiguration = new MetadataConfiguration(rootMap);

        } catch(IOException e) {
            LOGGER.error("Error while loading the config file: {}", e);
            throw new IllegalStateException("Error while loading the config file: " + e.getMessage());
        }
    }

    /**
     * Write the config information to file "config.yaml".
     *
     * <em>Attention:</em> All comments will be lost.
     */
    public void store() {
        store(new File("config.yaml"));
    }

    public void store(File file) {
        try {
            yaml.dump(rootMap, new FileWriter(file));
        } catch(IOException e) {
            LOGGER.error("Error while writing the configuration file: {}", e);
        }
    }

    public DatabaseConfiguration getDatabaseConfiguration() {
        return databaseConfiguration;
    }

    public GeneralConfiguration getGeneralConfiguration() {
        return generalConfiguration;
    }

    public InstrumentConfiguration getInstrumentConfiguration() {
        return instrumentConfiguration;
    }

    public MetadataConfiguration getMetadataConfiguration() {
        return metadataConfiguration;
    }
}
