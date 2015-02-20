package inspector.imondb.collector.view.gui.instrument;

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

import inspector.imondb.collector.model.RegexMap;
import inspector.imondb.collector.model.RegexSource;
import org.apache.commons.io.FilenameUtils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.util.Collection;

public class RegexMapTestPanel {

    private JPanel panel;

    private JTextField textFieldInput;

    private DefaultListModel<String> listModel;
    private JList<String> listMatches;

    public RegexMapTestPanel() {
    }

    public RegexMapTestPanel(Collection<? extends RegexMap> regexMaps) {
        this();

        textFieldInput.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                update();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                update();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                update();
            }

            private void update() {
                // remove old matches
                listModel.clear();

                // add new matches
                String fileName = FilenameUtils.getName(textFieldInput.getText());
                String filePath = FilenameUtils.getFullPath(textFieldInput.getText());

                regexMaps.stream().filter(regexMap ->
                        (regexMap.getSource() == RegexSource.NAME && fileName.matches(regexMap.getRegex())) ||
                        (regexMap.getSource() == RegexSource.PATH && filePath.matches(regexMap.getRegex()))).
                        forEach(regexMap -> listModel.addElement(regexMap.getKey()));
            }
        });
    }

    private void createUIComponents() {
        listModel = new DefaultListModel<>();
        listMatches = new JList<>(listModel);
    }

    public JPanel getPanel() {
        return panel;
    }
}
