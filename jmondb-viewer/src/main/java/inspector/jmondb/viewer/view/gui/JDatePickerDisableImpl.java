package inspector.jmondb.viewer.view.gui;

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

/**
 Copyright 2004 Juan Heyns. All rights reserved.

 Redistribution and use in source and binary forms, with or without modification, are
 permitted provided that the following conditions are met:

 1. Redistributions of source code must retain the above copyright notice, this list of
 conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice, this list
 of conditions and the following disclaimer in the documentation and/or other materials
 provided with the distribution.

 THIS SOFTWARE IS PROVIDED BY JUAN HEYNS ``AS IS'' AND ANY EXPRESS OR IMPLIED
 WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL JUAN HEYNS OR
 CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 The views and conclusions contained in the software and documentation are those of the
 authors and should not be interpreted as representing official policies, either expressed
 or implied, of Juan Heyns.
 */

import org.jdatepicker.DateModel;
import org.jdatepicker.JDatePanel;
import org.jdatepicker.JDatePicker;
import org.jdatepicker.impl.JDatePanelImpl;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Calendar;


/*
 * Temporary class duplication because of bugs in the JDatePicker code:
 * https://github.com/JDatePicker/JDatePicker/issues/46
 */
public class JDatePickerDisableImpl extends JPanel implements JDatePicker {

    private static final long serialVersionUID = 2814777654384974503L;

    private Popup popup;
    private JFormattedTextField formattedTextField;
    private JButton button;

    private JDatePanelImpl datePanel;

    public JDatePickerDisableImpl(JDatePanelImpl datePanel, JFormattedTextField.AbstractFormatter formatter) {
        this.datePanel = datePanel;

        //Initialise Variables
        popup = null;
        datePanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        InternalEventHandler internalEventHandler = new InternalEventHandler();

        //Create Layout
        SpringLayout layout = new SpringLayout();
        setLayout(layout);

        //Create and Add Components
        //Add and Configure TextField
        formattedTextField = new JFormattedTextField(formatter);
        DateModel<?> model = datePanel.getModel();
        setTextFieldValue(formattedTextField, model.getYear(), model.getMonth(), model.getDay(), model.isSelected());
        formattedTextField.setEditable(false);
        add(formattedTextField);
        layout.putConstraint(SpringLayout.WEST, formattedTextField, 0, SpringLayout.WEST, this);
        layout.putConstraint(SpringLayout.SOUTH, this, 0, SpringLayout.SOUTH, formattedTextField);

        //Add and Configure Button
        button = new JButton("...");
        button.setFocusable(true);
        add(button);
        layout.putConstraint(SpringLayout.WEST, button, 1, SpringLayout.EAST, formattedTextField);
        layout.putConstraint(SpringLayout.EAST, this, 0, SpringLayout.EAST, button);
        layout.putConstraint(SpringLayout.SOUTH, this, 0, SpringLayout.SOUTH, button);

        //Do layout formatting
        int h = (int)button.getPreferredSize().getHeight();
        int w = (int)datePanel.getPreferredSize().getWidth();
        button.setPreferredSize(new Dimension(h, h));
        formattedTextField.setPreferredSize(new Dimension(w-h-1, h));

        //Add event listeners
        addHierarchyBoundsListener(internalEventHandler);
        button.addActionListener(internalEventHandler);
        formattedTextField.addPropertyChangeListener("value", internalEventHandler);
        datePanel.addActionListener(internalEventHandler);
        datePanel.getModel().addChangeListener(internalEventHandler);
    }

    public void addActionListener(ActionListener actionListener) {
        datePanel.addActionListener(actionListener);
    }

    public void removeActionListener(ActionListener actionListener) {
        datePanel.removeActionListener(actionListener);
    }

    public DateModel<?> getModel() {
        return datePanel.getModel();
    }

    public void setTextEditable(boolean editable) {
        formattedTextField.setEditable(editable);
    }

    public boolean isTextEditable() {
        return formattedTextField.isEditable();
    }

    public void setButtonFocusable(boolean focusable) {
        button.setFocusable(focusable);
    }

    public boolean getButtonFocusable() {
        return button.isFocusable();
    }

    public JDatePanel getJDateInstantPanel() {
        return datePanel;
    }

    public JFormattedTextField getJFormattedTextField() {
        return formattedTextField;
    }

    public void setEnabled(boolean enabled) {
        button.setEnabled(enabled);
        formattedTextField.setEnabled(enabled);
    }

    private void showPopup() {
        if (popup == null){
            PopupFactory fac = new PopupFactory();
            Point xy = getLocationOnScreen();
            datePanel.setVisible(true);
            popup = fac.getPopup(this, datePanel, (int) xy.getX(), (int) (xy.getY()+this.getHeight()));
            popup.show();
        }
    }

    private void hidePopup() {
        if (popup != null) {
            popup.hide();
            popup = null;
        }
    }

    private class InternalEventHandler implements ActionListener, HierarchyBoundsListener, ChangeListener, PropertyChangeListener {

        public void ancestorMoved(HierarchyEvent arg0) {
            hidePopup();
        }

        public void ancestorResized(HierarchyEvent arg0) {
            hidePopup();
        }

        public void actionPerformed(ActionEvent arg0) {
            if (arg0.getSource() == button){
                if (popup == null) {
                    showPopup();
                }
                else {
                    hidePopup();
                }
            }
            else if (arg0.getSource() == datePanel){
                hidePopup();
            }
        }

        public void stateChanged(ChangeEvent arg0) {
            if (arg0.getSource() == datePanel.getModel()) {
                DateModel<?> model = datePanel.getModel();
                setTextFieldValue(formattedTextField, model.getYear(), model.getMonth(), model.getDay(), model.isSelected());
            }
        }

        public void propertyChange(PropertyChangeEvent evt) {
            if (formattedTextField.isEditable() && formattedTextField.getValue() != null) {
                Calendar value = (Calendar)formattedTextField.getValue();
                datePanel.getModel().setDate(value.get(Calendar.YEAR), value.get(Calendar.MONTH), value.get(Calendar.DATE));
                datePanel.getModel().setSelected(true);
            }
        }

    }

    public boolean isDoubleClickAction() {
        return datePanel.isDoubleClickAction();
    }

    public boolean isShowYearButtons() {
        return datePanel.isShowYearButtons();
    }

    public void setDoubleClickAction(boolean doubleClickAction) {
        datePanel.setDoubleClickAction(doubleClickAction);
    }

    public void setShowYearButtons(boolean showYearButtons) {
        datePanel.setShowYearButtons(showYearButtons);
    }

    private void setTextFieldValue(JFormattedTextField textField, int year, int month, int day, boolean isSelected) {
        if (!isSelected) {
            textField.setValue(null);
        }
        else {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, day, 0, 0, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            textField.setValue(calendar);
        }
    }

}
