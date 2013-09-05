package org.xendan.logmonitor.idea;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import com.jgoodies.common.collect.ArrayListModel;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.xendan.logmonitor.dao.LogMonitorSettingsDao;
import org.xendan.logmonitor.model.LogMonitorConfiguration;
import org.xendan.logmonitor.model.ServerSettings;
import org.xendan.logmonitor.read.ReaderScheduler;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

/**
 * User: id967161
 * Date: 03/09/13
 */
public class LogMonitorSettingsConfigurable implements SearchableConfigurable, Configurable.NoScroll {
    private final LogMonitorConfiguration config;
    private final LogMonitorSettingsDao logMonitorSettignsDao;
    private final ReaderScheduler scheduler;
    private ArrayListModel<ServerSettings> settingsModel;
    private LogMonitorConfiguration initialConfig;
    private JPanel contentPanel;
    private JList environmentList;
    private JTextField nameTextField;
    private JButton removeButton;
    private JTextField hostTextField;
    private JTextField loginTextField;
    private JTextField passwordTextField;
    private JTextField pathTextField;
    private JButton addButton;

    @NotNull
    @Override
    public String getId() {
        return "logmonitor.settings";
    }

    public LogMonitorSettingsConfigurable(Project project) {
        logMonitorSettignsDao = ServiceManager.getService(LogMonitorSettingsDao.class);
        scheduler = ServiceManager.getService(ReaderScheduler.class);
        this.config = logMonitorSettignsDao.getConfig(project.getName());
        addButton.addActionListener(new AddListener());
        removeButton.addActionListener(new RemoveListener());
    }

    private void resetInitial() {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(config);
            out.flush();
            out.close();
            ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
            initialConfig = (LogMonitorConfiguration) in.readObject();
        }
        catch(Exception e) {
            throw new IllegalStateException("Error creating copy", e);
        }
    }

    @Nullable
    @Override
    public Runnable enableSearch(String s) {
        return null;
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "Log monitor settings";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        //TODO check why..
        return "settings.logmonitor";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        return contentPanel;
    }

    @Override
    public boolean isModified() {
        return !initialConfig.equals(config);
    }

    @Override
    public void apply() throws ConfigurationException {
        resetInitial();
        logMonitorSettignsDao.save(config);
        scheduler.reload();
    }

    @Override
    public void reset() {
        resetInitial();
        settingsModel = new ArrayListModel<ServerSettings>(config.getServerSettings());
        environmentList.setModel(settingsModel);
    }

    @Override
    public void disposeUIResources() {
    }


    private class AddListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            config.getServerSettings().add(createServerSettings());
            refreshSettingsModel();
            clear();
        }

        private void clear() {
            nameTextField.setText("");
            passwordTextField.setText("");
            hostTextField.setText("");
            loginTextField.setText("");
            pathTextField.setText("");
        }

        private ServerSettings createServerSettings() {
            ServerSettings settings = new ServerSettings();
            settings.setName(nameTextField.getText());
            settings.setPassword(passwordTextField.getText());
            settings.setHost(hostTextField.getText());
            settings.setLogin(loginTextField.getText());
            settings.setPath(pathTextField.getText());
            return settings;
        }
    }

    private void refreshSettingsModel() {
        settingsModel.clear();
        settingsModel.addAll(config.getServerSettings());
    }

    private class RemoveListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            config.getServerSettings().remove(environmentList.getSelectedValue());
            refreshSettingsModel();
        }
    }
}
