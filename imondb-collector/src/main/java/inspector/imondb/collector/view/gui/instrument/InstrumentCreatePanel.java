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

import inspector.imondb.collector.model.InstrumentMap;
import inspector.imondb.collector.model.RegexSource;
import inspector.imondb.model.InstrumentModel;
import org.apache.commons.lang.WordUtils;

import javax.swing.*;

public class InstrumentCreatePanel {

    private JPanel panel;

    private JTextField textFieldName;
    private JComboBox<String> comboBoxModel;
    private JComboBox<String> comboBoxSource;
    private JTextField textFieldRegex;

    public InstrumentCreatePanel() {
        for(InstrumentModel instrumentModel : InstrumentModel.values()) {
            String modelName = WordUtils.capitalizeFully(instrumentModel.name(), new char[] { '_' }).replaceAll("_", " ");
            String modelAccession = instrumentModel.toString();
            String item = modelName + " (" + modelAccession + ")";
            comboBoxModel.addItem(item);

            if(instrumentModel == InstrumentModel.UNKNOWN_MODEL) {
                comboBoxModel.setSelectedItem(item);
            }
        }

        for(RegexSource regexSource : RegexSource.values()) {
            comboBoxSource.addItem(regexSource.toString());
        }
    }

    public InstrumentCreatePanel(InstrumentMap instrumentMap) {
        this();

        textFieldName.setText(instrumentMap.getKey());
        textFieldName.setEnabled(false);

        String modelName = WordUtils.capitalizeFully(instrumentMap.getValue().name(), new char[] { '_' }).replaceAll("_", " ");
        String modelAccession = instrumentMap.getValue().toString();
        comboBoxModel.setSelectedItem(modelName + " (" + modelAccession + ")");

        comboBoxSource.setSelectedItem(instrumentMap.getSource().toString());

        textFieldRegex.setText(instrumentMap.getRegex());
    }

    public JPanel getPanel() {
        return panel;
    }

    public InstrumentMap getInstrumentMap() {
        String modelStr = (String) comboBoxModel.getSelectedItem();
        InstrumentModel model = InstrumentModel.fromString(modelStr.substring(modelStr.lastIndexOf('(') + 1, modelStr.lastIndexOf(')')));
        RegexSource source = RegexSource.fromString((String) comboBoxSource.getSelectedItem());
        return new InstrumentMap(textFieldName.getText(), model, source, textFieldRegex.getText());
    }
}
