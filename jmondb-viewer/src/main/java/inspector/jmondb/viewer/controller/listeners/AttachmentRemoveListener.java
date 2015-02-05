package inspector.jmondb.viewer.controller.listeners;

/*
 * #%L
 * jMonDB Viewer
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

import inspector.jmondb.viewer.view.gui.EventDialog;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AttachmentRemoveListener implements ActionListener {

    private EventDialog eventDialog;

    public AttachmentRemoveListener(EventDialog eventDialog) {
        this.eventDialog = eventDialog;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(eventDialog.getAttachmentName() != null || eventDialog.getAttachmentContent() != null) {
            eventDialog.setAttachmentName("No attachment added");
            eventDialog.setAttachmentContent(null);
            eventDialog.setAttachmentIconFileType();    // no-file icon
        }
    }
}
