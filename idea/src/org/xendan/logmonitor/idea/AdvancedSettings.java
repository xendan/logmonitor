package org.xendan.logmonitor.idea;

import org.apache.commons.io.FileUtils;
import org.xendan.logmonitor.HomeResolver;
import org.xendan.logmonitor.read.command.CommandFileLoader;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

/**
 * User: id967161
 * Date: 22/11/13
 */
public class AdvancedSettings {

    private final HomeResolver homeResolver;
    private final CommandFileLoader filterCommandLoader;
    private JCheckBox useDefaultCheckBox;
    private JPanel contentPanel;
    private JTextArea scriptTextArea;
    private final OnOkAction onOkAction;

    public AdvancedSettings(HomeResolver homeResolver) {
        this.homeResolver = homeResolver;
        this.onOkAction = new DoCommitChanges();
        this.filterCommandLoader = CommandFileLoader.createFilter(homeResolver);
        init();
    }

    private void init() {
        useDefaultCheckBox.setSelected(!filterCommandLoader.getCommandFile().exists());
        onUseDefaultSelected();
        useDefaultCheckBox.addActionListener(new IsDefaultCheckBox());
    }

    private void onUseDefaultSelected() {
            scriptTextArea.setEnabled(!useDefaultCheckBox.isSelected());
            scriptTextArea.setText(getTextAreaText());
        }

    private String getTextAreaText() {
        File file = filterCommandLoader.getCommandFile();
        if (useDefaultCheckBox.isSelected() || !file.exists())  {
            return filterCommandLoader.readCommandFromResource();
        }
        return filterCommandLoader.readCommandFromFile(file);
    }


    public JPanel getContentPanel() {
        return contentPanel;
    }

    public OnOkAction getOnOkAction() {
        return onOkAction;
    }


    private class IsDefaultCheckBox implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            onUseDefaultSelected();
        }
    }

    private class DoCommitChanges implements OnOkAction {
        @Override
        public boolean canClose() {
            File file = filterCommandLoader.getCommandFile();
            if (useDefaultCheckBox.isSelected()) {
                if (file.exists() && !file.delete()) {
                    throw new IllegalStateException("Error deleting file " + file);
                }
            } else {
                try {
                    FileUtils.writeStringToFile(file, scriptTextArea.getText());
                } catch (IOException e) {
                    throw new IllegalStateException("Error writing to file " + file, e);
                }
            }
            return true;
        }
    }
}
