package inspector.imondb.collector.view.gui.metadata;

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

import inspector.imondb.collector.model.MetadataMap;
import inspector.imondb.collector.model.RegexSource;

import javax.swing.*;

public class MetadataCreatePanel {

    private JPanel panel;

    private JTextField textFieldKey;
    private JTextField textFieldValue;
    private JTextField textFieldRegex;
    private JComboBox<String> comboBoxSource;

    public MetadataCreatePanel() {
        for(RegexSource regexSource : RegexSource.values()) {
            comboBoxSource.addItem(regexSource.toString());
        }
    }

    public MetadataCreatePanel(MetadataMap metadataMap) {
        this();

        textFieldKey.setText(metadataMap.getKey());
        textFieldKey.setEnabled(false);

        textFieldValue.setText(metadataMap.getValue());
        textFieldValue.setEnabled(false);

        comboBoxSource.setSelectedItem(metadataMap.getSource().toString());

        textFieldRegex.setText(metadataMap.getRegex());
    }

    public JPanel getPanel() {
        return panel;
    }

    public MetadataMap getMetadataMap() {
        RegexSource source = RegexSource.fromString((String) comboBoxSource.getSelectedItem());
        return new MetadataMap(textFieldKey.getText(), textFieldValue.getText(), source, textFieldRegex.getText());
    }
}
