package org.xendan.logmonitor.idea;

import org.apache.commons.io.FileUtils;
import org.xendan.logmonitor.HomeResolver;
import org.xendan.logmonitor.model.OnOkAction;
import org.xendan.logmonitor.read.command.LogDownloader;

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
    private JCheckBox useDefaultCheckBox;
    private JPanel contentPanel;
    private JTextArea scriptTextArea;
    private final OnOkAction onOkAction;

    public AdvancedSettings(HomeResolver homeResolver) {
        this.homeResolver = homeResolver;
        this.onOkAction = new DoCommitChanges();
        init();
    }

    private void init() {
        useDefaultCheckBox.setSelected(!LogDownloader.getCommandFile(homeResolver).exists());
        onUseDefaultSelected();
        useDefaultCheckBox.addActionListener(new IsDefaultCheckBox());
    }

    private void onUseDefaultSelected() {
            scriptTextArea.setEnabled(!useDefaultCheckBox.isSelected());
            scriptTextArea.setText(getTextAreaText());
        }

    private String getTextAreaText() {
        File file = LogDownloader.getCommandFile(homeResolver);
        if (useDefaultCheckBox.isSelected() || !file.exists())  {
            return LogDownloader.readCommandFromResource();
        }
        return LogDownloader.readCommandFromFile(file);
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
            File file = LogDownloader.getCommandFile(homeResolver);
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
