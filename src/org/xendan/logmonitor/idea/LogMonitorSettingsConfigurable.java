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
import org.xendan.logmonitor.model.LogSettings;
import org.xendan.logmonitor.model.MatchConfig;
import org.xendan.logmonitor.model.Server;
import org.xendan.logmonitor.read.ReaderScheduler;
import org.xendan.logmonitor.read.Serializer;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * User: id967161
 * Date: 03/09/13
 */
public class LogMonitorSettingsConfigurable implements SearchableConfigurable, Configurable.NoScroll {
    private final List<LogMonitorConfiguration> configs;
    private final Serializer serializer;
    private final LogMonitorSettingsDao logMonitorSettignsDao;
    private final ReaderScheduler scheduler;
    private final Project project;
    private ArrayListModel<LogSettings> settingsModel;
    private  List<LogMonitorConfiguration> initialConfigs;
    private JPanel contentPanel;
    private JButton removeButton;

    private JTextField pathTextField;
    private JButton addButton;
    private JButton addPatternButton;
    private JButton removePatternButton;
    private JComboBox levelComboBox;
    private JScrollPane patternsListPanel;
    private JTextField mathcerNameTextField;
    private JTextField patternTextField;
    private JComboBox projectComboBox;
    private JLabel projectLabel;
    private JTextField matchMessageTextField;
    private JCheckBox userArchiveCheckBox;
    private JPasswordField passwordField;
    private JTextArea messageTextArea;
    private JList logSettingsList;
    private JComboBox serverComboBox;
    private JPanel serverPanel;
    private JButton saveLogSettingsButton;
    private JTextField serverHostTextField;
    private JTextField serverLoginTextField;
    private JPasswordField serverPasswordField;
    private JTextField keyFiletextField;
    private JButton selectFileButton;
    private JTree patternsTree;
    private JLabel serverLabel;
    private JLabel pathLabel;
    private JButton patternUp;
    private JButton patternDown;
    private JTextField logSettingsNametextField;
    private JButton addProjectButton;
    private JTextField projectNameTextField;
    private JLabel logSettingsLabel;
    private JPanel serverPanelBig;
    private JSpinner updateIntervalSpinner;
    private JButton broswsLogButton;
    private JPasswordField serverPastPhrsePasswordField;
    private JLabel updateIntrevalLabel;
    private Server ADD_NEW = new Server("Add new...", -2);
    private Server selectedServer;
    private LogSettings selectedLogSettings;


    @NotNull
    @Override
    public String getId() {
        return "logmonitor.settings";
    }

    public LogMonitorSettingsConfigurable(Project project) {
        logMonitorSettignsDao = ServiceManager.getService(LogMonitorSettingsDao.class);
        serializer = ServiceManager.getService(Serializer.class);
        scheduler = ServiceManager.getService(ReaderScheduler.class);
        this.configs = logMonitorSettignsDao.getConfigs();
        addButton.addActionListener(new AddLogSettingsListener());
        addPatternButton.addActionListener(new AddPatternListener());
        removeButton.addActionListener(new RemoveListener());
        logSettingsList.addListSelectionListener(new LogSettingsSelectionListener());
        saveLogSettingsButton.addActionListener(new SaveLogSettingsButton());
        addProjectButton.addActionListener(new AddProjectActionListener());
        broswsLogButton.addActionListener(new BrowseLogButtonActionListener());
        this.project = project;
    }

    private Server[] getServers() {
        List<Server> servers = new ArrayList<Server>();
        servers.add(ADD_NEW);
        servers.add(Server.LOCALHOST);
        servers.addAll(logMonitorSettignsDao.getServers());
        return servers.toArray(new Server[servers.size()]);
    }

    private void resetInitial() {
        initialConfigs = serializer.doCopy(configs);
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
        return !initialConfigs.equals(configs);
    }

    @Override
    public void apply() throws ConfigurationException {
        resetInitial();
        selectedConfig().setLogPattern(patternTextField.getText());
        logMonitorSettignsDao.save(configs);
        scheduler.reload();
    }

    private LogMonitorConfiguration selectedConfig() {
        return findByProject(projectComboBox.getSelectedItem().toString());
    }

    private LogMonitorConfiguration findByProject(String projectName) {
        for (LogMonitorConfiguration config : configs) {
            if (config.getProjectName().equals(projectName)) {
                return config;
            }
        }
        return null;
    }

    @Override
    public void reset() {
        resetInitial();
        refresh();
    }

    private void refresh() {
        projectComboBox.setModel(new DefaultComboBoxModel(getProjects()));
        projectComboBox.setSelectedItem(project.getName());
        settingsModel = new ArrayListModel<LogSettings>(selectedConfig().getLogSettings());
        patternTextField.setText(selectedConfig().getLogPattern());
        logSettingsList.setModel(settingsModel);
        serverComboBox.setModel(new DefaultComboBoxModel(getServers()));
        serverComboBox.addActionListener(new ServerComboListener());
        setLogSettingsEnable(false);
        setProjectButtonCreate();
        projectNameTextField.setText("");
    }

    @Override
    public void disposeUIResources() {
    }

    private void setLogSettingsEnable(boolean enabled) {
        logSettingsLabel.setEnabled(enabled);
        logSettingsNametextField.setEnabled(enabled);
        serverLabel.setEnabled(enabled);
        JPanel panel = serverPanelBig;
        enablePanel(enabled, panel);
        pathLabel.setEnabled(enabled);
        pathTextField.setEnabled(enabled);
        saveLogSettingsButton.setEnabled(enabled);
        updateIntervalSpinner.setEnabled(enabled);
        updateIntrevalLabel.setEnabled(enabled);
    }

    private void enablePanel(boolean enabled, JPanel panel) {
        for (Component component : panel.getComponents()) {
               component.setEnabled(enabled);
               if (component instanceof JPanel) {
                   enablePanel(enabled, (JPanel) component);
               }
        }
    }

    private void refreshSettingsModel() {
        settingsModel.clear();
        settingsModel.addAll(selectedConfig().getLogSettings());
    }

    private void refreshMatchers() {
        List<MatchConfig> matchConfigs = selectedLogSettings == null ? new ArrayList<MatchConfig>() : selectedLogSettings.getMatchConfigs();
        Collections.sort(matchConfigs);
        //mathcerList.setModel(new ArrayListModel<MatchConfig>(matchConfigs));
    }

    public String[] getProjects() {
        List<String> projects = new ArrayList<String>();
        for (LogMonitorConfiguration config : configs) {
            projects.add(config.getProjectName());
        }
        if (!projects.contains(project.getName())) {
            projects.add(project.getName());
            LogMonitorConfiguration config = new LogMonitorConfiguration();
            config.setProjectName(project.getName());
            configs.add(config);
        }
        return projects.toArray(new String[projects.size()]);
    }


    private class RemoveListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            selectedConfig().getLogSettings().remove(selectedLogSettings);
            refreshSettingsModel();
        }
    }


    private void setProjectButtonCreate() {
        projectNameTextField.setVisible(false);
        addProjectButton.setText("Create new");
    }

    private class AddLogSettingsListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            selectedLogSettings = new LogSettings();
            setLogSettingsEnable(true);
            logSettingsList.clearSelection();
            pathTextField.setText("");
            updateIntervalSpinner.setValue(5);
            serverComboBox.setSelectedItem(ADD_NEW);
            saveLogSettingsButton.setText("Add new");
        }
    }

    private class LogSettingsSelectionListener implements ListSelectionListener {
        @Override
        public void valueChanged(ListSelectionEvent e) {
            selectedLogSettings = (LogSettings) logSettingsList.getSelectedValue();
            if (selectedLogSettings != null) {
               setLogSettingsEnable(true);
               pathTextField.setText(selectedLogSettings.getPath());
               logSettingsNametextField.setText(selectedLogSettings.getName());
                if (selectedLogSettings.getServer() == null) {
                    serverComboBox.setSelectedItem(Server.LOCALHOST);
                } else {
                    serverComboBox.setSelectedItem(selectedLogSettings.getServer());
                }
               saveLogSettingsButton.setText("Save");
               updateIntervalSpinner.setValue(selectedLogSettings.getUpdateInterval());
            }
        }
    }

    private class AddPatternListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            //getSelectedLogSettings().getMatchConfigs().add(createMatcher());
            mathcerNameTextField.setText("");
            refreshMatchers();
        }


    }

    private class SaveLogSettingsButton implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            selectedLogSettings.setName(logSettingsNametextField.getText());
            selectedLogSettings.setPath(pathTextField.getText());
            selectedLogSettings.setServer(getSelectedServer());
            selectedLogSettings.setUpdateInterval((Integer) updateIntervalSpinner.getValue());
            if (!selectedConfig().getLogSettings().contains(selectedLogSettings)) {
                selectedConfig().getLogSettings().add(selectedLogSettings);
            }
            setLogSettingsEnable(false);
            pathTextField.setText("");
            logSettingsNametextField.setText("");
            setServerData(new Server());
            selectedLogSettings = null;
            selectedConfig().setLogPattern(patternTextField.getText());
            refresh();
        }
    }

    private void setServerData(Server server) {
        if (server == null) {
            return;
        }
        serverHostTextField.setText(server.getHost());
        keyFiletextField.setText(server.getKeyPath());
        serverLoginTextField.setText(server.getLogin());
        serverPasswordField.setText(server.getPassword());
        serverPastPhrsePasswordField.setText(server.getPassPhrase());
    }

    private class ServerComboListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            selectedServer = (Server) serverComboBox.getSelectedItem();
            if (selectedServer == Server.LOCALHOST) {
                selectedServer = null;
            }
            if (selectedServer == ADD_NEW) {
                selectedServer = new Server();
            }
            serverPanel.setVisible(selectedServer != null);
            setServerData(selectedServer);
        }
    }

    private Server getSelectedServer() {
        if (selectedServer == null) {
            return null;
        }
        selectedServer.setHost(serverHostTextField.getText());
        selectedServer.setKeyPath(keyFiletextField.getText());
        selectedServer.setLogin(serverLoginTextField.getText());
        selectedServer.setPassword(new String(serverPasswordField.getPassword()));
        selectedServer.setPassPhrase(new String(serverPastPhrsePasswordField.getPassword()));
        return selectedServer;
    }


    private class AddProjectActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (projectNameTextField.isVisible()) {
                LogMonitorConfiguration config = new LogMonitorConfiguration();
                String name = projectNameTextField.getName();
                config.setProjectName(name);
                refresh();
                projectComboBox.setSelectedItem(name);

            } else {
                projectNameTextField.setVisible(true);
                addProjectButton.setText("Add project");
            }
        }
    }

    private class BrowseLogButtonActionListener implements ActionListener, LogChooseListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            Server server = getSelectedServer();
            if (server == null) {
                final JFileChooser fc = new JFileChooser();
                fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                int returnVal = fc.showOpenDialog(contentPanel);

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    pathTextField.setText(file.getAbsolutePath());
                }
            } else {
                ServerLogChooser chooser = new ServerLogChooser(server, this);
                chooser.setSize(300,300);
                chooser.setMinimumSize(new Dimension(300, 300));
                chooser.setLocationRelativeTo(null);
                chooser.pack();
                chooser.setVisible(true);
            }
        }

        @Override
        public void onFileSelected(String path) {
            pathTextField.setText(path);
        }
    }
}
