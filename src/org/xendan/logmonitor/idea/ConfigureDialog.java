package org.xendan.logmonitor.idea;

import com.intellij.openapi.options.ConfigurationException;

import javax.swing.*;
import java.awt.event.*;

public class ConfigureDialog extends JDialog {
    private final LogMonitorSettingsConfigurable logMonitorSettingsConfigurable;
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JPanel infoPanel;

    public ConfigureDialog(LogMonitorSettingsConfigurable logMonitorSettingsConfigurable) {
        this.logMonitorSettingsConfigurable = logMonitorSettingsConfigurable;
        logMonitorSettingsConfigurable.reset();
        setContentPane(contentPane);
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.PAGE_AXIS));
        infoPanel.add(logMonitorSettingsConfigurable.getContentPanel());
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
        try {
            logMonitorSettingsConfigurable.apply();
            dispose();
        } catch (ConfigurationException e) {
            //TODO show message
        }

    }

    private void onCancel() {
        dispose();
    }
}
