package inspector.jmondb.viewer.controller.listeners;

import inspector.jmondb.viewer.controller.DatabaseController;

import javax.swing.*;
import java.awt.event.*;

public class ExitAction extends AbstractAction {

    private DatabaseController databaseController;

    public ExitAction(DatabaseController databaseController) {
        this.databaseController = databaseController;

        putValue(Action.NAME, "Exit");
        putValue(Action.MNEMONIC_KEY, KeyEvent.VK_X);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        databaseController.disconnect();

        System.exit(0);
    }
}
