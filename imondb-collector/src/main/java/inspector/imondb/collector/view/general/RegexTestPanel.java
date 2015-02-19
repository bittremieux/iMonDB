package inspector.imondb.collector.view.general;

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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class RegexTestPanel {

    private static final ImageIcon iconMatch = new ImageIcon(RegexTestPanel.class.getResource("/images/ok.png"));
    private static final ImageIcon iconNoMatch = new ImageIcon(RegexTestPanel.class.getResource("/images/nok.png"));

    private JPanel panel;

    private JTextField textFieldRegex;
    private JTextField textFieldInput;
    private JLabel labelResult;

    public RegexTestPanel() {
        RegexListener listener = new RegexListener();
        textFieldRegex.getDocument().addDocumentListener(listener);
        textFieldInput.getDocument().addDocumentListener(listener);
    }

    public RegexTestPanel(String regex) {
        this();

        textFieldRegex.setText(regex);
    }

    public JPanel getPanel() {
        return panel;
    }

    public String getRegex() {
        return textFieldRegex.getText();
    }

    private class RegexListener implements DocumentListener {
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
            boolean matches = textFieldInput.getText().matches(textFieldRegex.getText());
            labelResult.setIcon(matches ? iconMatch : iconNoMatch);
            labelResult.setText(matches ? "match" : "no match");
        }
    }
}
