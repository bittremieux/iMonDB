package inspector.imondb.collector.view.update;

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

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class UpdateListener implements ActionListener {

    private final String applicationId;
    private final String applicationName;

    public UpdateListener(String applicationId, String applicationName) {
        this.applicationId = applicationId;
        this.applicationName = applicationName;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        boolean isSilent = "auto update".equals(e.getActionCommand());
        SwingWorker<String, Void> worker = new UpdateWorker(applicationId, applicationName, isSilent);
        worker.execute();
    }


}
