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

import inspector.imondb.collector.controller.DatabaseController;
import inspector.imondb.collector.model.InstrumentMap;
import inspector.imondb.collector.view.gui.CollectorFrame;
import inspector.imondb.collector.view.gui.database.DatabasePanel;
import inspector.imondb.collector.view.gui.instrument.InstrumentOverviewPanel;
import inspector.imondb.collector.view.gui.instrument.InstrumentsPanel;
import inspector.imondb.model.Instrument;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

public class DatabaseConnectionListener implements ActionListener, DocumentListener, Observer {

    private CollectorFrame collectorFrame;

    private DatabaseController databaseController;

    public DatabaseConnectionListener(CollectorFrame collectorFrame, DatabaseController databaseController) {
        this.collectorFrame = collectorFrame;
        this.databaseController = databaseController;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        DatabasePanel databasePanel = collectorFrame.getDatabasePanel();
        InstrumentsPanel instrumentsPanel = collectorFrame.getInstrumentsPanel();

        // set unknown status
        collectorFrame.setWaitCursor(true);
        databasePanel.setConnectionStatus(DatabasePanel.ConnectionStatus.IN_PROGRESS);

        Collection<InstrumentMap> instrumentMaps = instrumentsPanel.getInstruments();
        for(InstrumentMap instrumentMap : instrumentMaps) {
            instrumentsPanel.setInstrumentStatus(instrumentMap, InstrumentOverviewPanel.InstrumentStatus.UNKNOWN);
        }

        // connect to the database
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                // establish the connection
                databaseController.connectTo(databasePanel.getHost(), databasePanel.getPort(),
                        databasePanel.getDatabase(), databasePanel.getUserName(), databasePanel.getPassword());

                // verify the existence of the instruments in the database
                instrumentMaps.forEach(DatabaseConnectionListener.this::updateInstrument);

                return null;
            }

            @Override
            protected void done() {
                DatabasePanel databasePanel = collectorFrame.getDatabasePanel();

                try {
                    get();

                    // connection succeeded
                    databasePanel.setConnectionStatus(DatabasePanel.ConnectionStatus.CONNECTED);
                } catch(InterruptedException | ExecutionException ex) {
                    // connection failed
                    databasePanel.setConnectionStatus(DatabasePanel.ConnectionStatus.FAILED_CONNECTION);

                    JOptionPane.showMessageDialog(Frame.getFrames()[0], "<html><b>Could not connect to the database</b></html>\n"
                            + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                } catch(CancellationException ignored) {
                }

                collectorFrame.setWaitCursor(false);

                collectorFrame.getOverviewPanel().update();
            }
        };

        // connect to the database
        worker.execute();
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        disconnect();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        disconnect();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        disconnect();
    }

    private void disconnect() {
        databaseController.disconnect();

        // reset database information
        collectorFrame.getDatabasePanel().setConnectionStatus(DatabasePanel.ConnectionStatus.UNKNOWN);

        // reset instruments
        InstrumentsPanel instrumentsPanel = collectorFrame.getInstrumentsPanel();
        for(InstrumentMap instrumentMap : instrumentsPanel.getInstruments()) {
            instrumentsPanel.setInstrumentStatus(instrumentMap, InstrumentOverviewPanel.InstrumentStatus.UNKNOWN);
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        // update a new instrument
        if(arg != null) {
            updateInstrument((InstrumentMap) arg);
        }
        // no arg if an instrument was removed

        collectorFrame.getOverviewPanel().update();
    }

    private void updateInstrument(InstrumentMap instrumentMap) {
        if(databaseController.isActive()) {
            Instrument instrument = databaseController.getReader().getInstrument(instrumentMap.getKey());
            InstrumentOverviewPanel.InstrumentStatus status;
            if(instrument != null && instrument.getType().equals(instrumentMap.getValue())) {
                status = InstrumentOverviewPanel.InstrumentStatus.VALID;
            } else if(instrument != null && !instrument.getType().equals(instrumentMap.getValue())) {
                status = InstrumentOverviewPanel.InstrumentStatus.INVALID;
            } else if(instrument == null) {
                status = InstrumentOverviewPanel.InstrumentStatus.NEW;
            } else {
                status = InstrumentOverviewPanel.InstrumentStatus.UNKNOWN;
            }

            collectorFrame.getInstrumentsPanel().setInstrumentStatus(instrumentMap, status);
        }
    }
}
