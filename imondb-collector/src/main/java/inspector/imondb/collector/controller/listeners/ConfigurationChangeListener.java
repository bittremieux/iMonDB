package inspector.imondb.collector.controller.listeners;

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

import inspector.imondb.collector.model.config.Configuration;
import inspector.imondb.collector.view.gui.CollectorFrame;
import inspector.imondb.collector.view.gui.external.ExternalPanel;
import inspector.imondb.collector.view.gui.instrument.InstrumentsPanel;
import inspector.imondb.collector.view.gui.metadata.MetadataPanel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.util.Observable;
import java.util.Observer;

public class ConfigurationChangeListener implements FocusListener, ActionListener, ChangeListener, Observer {

    private CollectorFrame collectorFrame;

    private Configuration configuration;

    public ConfigurationChangeListener(CollectorFrame collectorFrame, Configuration configuration) {
        this.collectorFrame = collectorFrame;
        this.configuration = configuration;
    }

    @Override
    public void focusGained(FocusEvent e) {
        // do nothing
    }

    @Override
    public void focusLost(FocusEvent e) {
        switch(((Component) e.getSource()).getName()) {
            // database configuration
            case "db_host":
                configuration.getDatabaseConfiguration().setHost(collectorFrame.getDatabasePanel().getHost());
                break;
            case "db_port":
                configuration.getDatabaseConfiguration().setPort(collectorFrame.getDatabasePanel().getPort());
                break;
            case "db_database":
                configuration.getDatabaseConfiguration().setDatabase(collectorFrame.getDatabasePanel().getDatabase());
                break;
            case "db_username":
                configuration.getDatabaseConfiguration().setUserName(collectorFrame.getDatabasePanel().getUserName());
                break;
            case "db_password":
                configuration.getDatabaseConfiguration().setPassword(collectorFrame.getDatabasePanel().getPassword());
                break;
            // general configuration
            case "gen_regex":
                configuration.getGeneralConfiguration().setFileNameRegex(collectorFrame.getGeneralPanel().getFileNameRegex());
                break;
            // external configuration
            case "sense_username":
                configuration.getSenseConfiguration().setUserName(collectorFrame.getExternalPanel().getUserName());
                break;
            case "sense_password":
                configuration.getSenseConfiguration().setPassword(collectorFrame.getExternalPanel().getPassword());
                break;
            default:
                break;
        }

        configuration.store();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch(((Component) e.getSource()).getName()) {
            // general configuration
            case "gen_dir":
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                fileChooser.setAcceptAllFileFilterUsed(false);
                int returnVal = fileChooser.showOpenDialog(collectorFrame.getGeneralPanel().getPanel());
                if(returnVal == JFileChooser.APPROVE_OPTION) {
                    File dir = fileChooser.getSelectedFile();
                    collectorFrame.getGeneralPanel().setDirectory(dir.getAbsolutePath());

                    configuration.getGeneralConfiguration().setDirectory(dir.getAbsolutePath());
                }
                break;
            case "gen_date":
                configuration.getGeneralConfiguration().setStartDate(collectorFrame.getGeneralPanel().getStartDate());
                break;
            case "gen_unique":
                configuration.getGeneralConfiguration().setUniqueFileNames(collectorFrame.getGeneralPanel().getEnforceUnique());
                break;
            default:
                break;
        }

        configuration.store();
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        if("gen_threads".equals(((Component) e.getSource()).getName())) {
            // general configuration
            configuration.getGeneralConfiguration().setNumberOfThreads(collectorFrame.getGeneralPanel().getNumberOfThreads());
        }

        configuration.store();
    }

    @Override
    public void update(Observable o, Object arg) {
        if(o instanceof InstrumentsPanel) {
            // instrument configuration
            configuration.getInstrumentConfiguration().setInstruments(collectorFrame.getInstrumentsPanel().getInstruments());
        } else if(o instanceof MetadataPanel) {
            // metadata configuration
            configuration.getMetadataConfiguration().setMetadata(collectorFrame.getMetadataPanel().getMetadata());
        } else if(o instanceof ExternalPanel) {
            // external configuration
            configuration.getSenseConfiguration().setDevices(collectorFrame.getExternalPanel().getDevices());
        }

        configuration.store();
    }
}
