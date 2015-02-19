package inspector.imondb.collector.view.overview;

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

import inspector.imondb.collector.model.InstrumentMap;
import inspector.imondb.collector.view.CollectorFrame;
import inspector.imondb.collector.view.database.DatabasePanel;
import inspector.imondb.collector.view.general.GeneralPanel;
import inspector.imondb.collector.view.instrument.InstrumentOverviewPanel;
import inspector.imondb.collector.view.instrument.InstrumentsPanel;
import inspector.imondb.collector.view.metadata.MetadataPanel;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Observable;

public class OverviewPanel extends Observable {

    private static ImageIcon iconError = new ImageIcon(OverviewPanel.class.getResource("/images/nok.png"));
    private static ImageIcon iconWarning = new ImageIcon(OverviewPanel.class.getResource("/images/warning.png"));
    private static ImageIcon iconValid = new ImageIcon(OverviewPanel.class.getResource("/images/ok.png"));

    public enum Status { VALID, WARNING, ERROR }

    private JPanel panel;

    private JLabel labelDatabase;
    private JLabel labelInstrument;
    private JLabel labelGeneral;
    private JLabel labelMetadata;

    private CollectorFrame collector;

    private Status globalStatus;

    public OverviewPanel(CollectorFrame collectorFrame) {
        this.collector = collectorFrame;
        this.globalStatus = Status.VALID;
    }

    public JPanel getPanel() {
        return panel;
    }

    public void update() {
        globalStatus = Status.VALID;

        // database configuration
        DatabasePanel databasePanel = collector.getDatabasePanel();
        switch(databasePanel.getConnectionStatus()) {
            case CONNECTED:
                setDatabaseStatus(Status.VALID, "Valid database configuration to " +
                        databasePanel.getUserName() + "@" + databasePanel.getHost() + ":" + databasePanel.getPort() +
                        "/" + databasePanel.getDatabase());
                break;
            case FAILED_CONNECTION:
                setDatabaseStatus(Status.ERROR, "Invalid database configuration");
                break;
            case IN_PROGRESS:
            case UNKNOWN:
                setDatabaseStatus(Status.WARNING, "Unverified database configuration");
            default:
                break;
        }

        // general configuration
        GeneralPanel generalPanel = collector.getGeneralPanel();
        if(generalPanel.getDirectory() == null) {
            setGeneralStatus(Status.ERROR, "No start directory set");
        } else if(StringUtils.isEmpty(generalPanel.getFileNameRegex())) {
            setGeneralStatus(Status.ERROR, "No raw file name regex set");
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("<html>Retrieving raw files matching \"").append(generalPanel.getFileNameRegex()).append("\"<br>")
                    .append("from \"").append(generalPanel.shortenPath(generalPanel.getDirectory()));
            if(generalPanel.getStartDate() != null) {
                sb.append("<br>modified after ").append((new SimpleDateFormat("dd/MM/yyyy")).format(generalPanel.getStartDate()));
            }
            sb.append("</html>");
            setGeneralStatus(Status.VALID, sb.toString());
        }

        // instrument configuration
        InstrumentsPanel instrumentsPanel = collector.getInstrumentsPanel();
        Collection<InstrumentMap> instrumentMaps = instrumentsPanel.getInstruments();
        if(instrumentMaps.size() == 0) {
            setInstrumentStatus(Status.ERROR, "No instruments configured");
        } else {
            InstrumentOverviewPanel.InstrumentStatus status = InstrumentOverviewPanel.InstrumentStatus.INVALID;
            for(InstrumentMap instrumentMap : instrumentMaps) {
                InstrumentOverviewPanel.InstrumentStatus thisStatus = instrumentsPanel.getInstrumentStatus(instrumentMap);
                status = status.compareTo(thisStatus) < 0 ? status : thisStatus;
            }
            switch(status) {
                case VALID:
                    setInstrumentStatus(Status.VALID, "One or more valid instrument configurations");
                    break;
                case INVALID:
                    setInstrumentStatus(Status.ERROR, "One or more invalid instrument configurations");
                    break;
                case UNKNOWN:
                case NEW:
                    setInstrumentStatus(Status.WARNING, "One or more unverified/new instrument configurations");
                default:
                    break;
            }
        }

        // metadata configuration
        MetadataPanel metadataPanel = collector.getMetadataPanel();
        if(metadataPanel.getMetadata().size() > 0) {
            setMetadataStatus(Status.VALID, "One or more valid metadata configurations");
        } else {
            setMetadataStatus(Status.WARNING, "No metadata configurations set");
        }

        setChanged();
        notifyObservers(globalStatus);
    }

    private void setDatabaseStatus(Status status, String message) {
        setIcon(labelDatabase, status);
        labelDatabase.setToolTipText(message);

        globalStatus = globalStatus.compareTo(status) < 0 ? status : globalStatus;
    }

    private void setGeneralStatus(Status status, String message) {
        setIcon(labelGeneral, status);
        labelGeneral.setToolTipText(message);

        globalStatus = globalStatus.compareTo(status) < 0 ? status : globalStatus;
    }

    private void setInstrumentStatus(Status status, String message) {
        setIcon(labelInstrument, status);
        labelInstrument.setToolTipText(message);

        globalStatus = globalStatus.compareTo(status) < 0 ? status : globalStatus;
    }

    private void setMetadataStatus(Status status, String message) {
        setIcon(labelMetadata, status);
        labelMetadata.setToolTipText(message);

        globalStatus = globalStatus.compareTo(status) < 0 ? status : globalStatus;
    }

    private void setIcon(JLabel label, Status status) {
        switch(status) {
            case VALID:
                label.setIcon(iconValid);
                break;
            case ERROR:
                label.setIcon(iconError);
                break;
            case WARNING:
            default:
                label.setIcon(iconWarning);
                break;
        }
    }
}
