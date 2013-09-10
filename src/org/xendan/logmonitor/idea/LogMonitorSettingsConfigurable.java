package org.xendan.logmonitor.idea;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import com.jgoodies.common.collect.ArrayListModel;
import org.apache.log4j.Level;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.xendan.logmonitor.dao.LogMonitorSettingsDao;
import org.xendan.logmonitor.model.LogMonitorConfiguration;
import org.xendan.logmonitor.model.MatchConfig;
import org.xendan.logmonitor.model.ServerSettings;
import org.xendan.logmonitor.read.MatcherService;
import org.xendan.logmonitor.read.ReaderScheduler;
import org.xendan.logmonitor.read.Serializer;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

/**
 * User: id967161
 * Date: 03/09/13
 */
public class LogMonitorSettingsConfigurable implements SearchableConfigurable, Configurable.NoScroll {
    private final LogMonitorConfiguration config;
    private final Serializer serializer;
    private final LogMonitorSettingsDao logMonitorSettignsDao;
    private final ReaderScheduler scheduler;
    private final MatcherService matcherService;
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
    private JButton addPatternButton;
    private JButton removePatternButton;
    private JCheckBox ignoreCheckBox;
    private JComboBox levelComboBox;
    private JScrollPane patternsListPanel;
    private JList mathcerList;
    private JLabel mathcerName;
    private JTextField mathcerNameTextField;
    private JTextField patternTextField;
    private JButton addProject;
    private JComboBox projectComboBox;
    private JLabel projectLabel;

    @NotNull
    @Override
    public String getId() {
        return "logmonitor.settings";
    }

    public LogMonitorSettingsConfigurable(Project project) {
        logMonitorSettignsDao = ServiceManager.getService(LogMonitorSettingsDao.class);
        matcherService = ServiceManager.getService(MatcherService.class);
        serializer = ServiceManager.getService(Serializer.class);
        scheduler = ServiceManager.getService(ReaderScheduler.class);
        this.config = logMonitorSettignsDao.getConfig(project.getName());
        addButton.addActionListener(new AddListener());
        addPatternButton.addActionListener(new AddPatternListener());
        removeButton.addActionListener(new RemoveListener());
        environmentList.addListSelectionListener(new ServerSelectionListener());
        levelComboBox.setModel(new DefaultComboBoxModel(new Level[]{Level.FATAL, Level.ERROR, Level.WARN, Level.INFO, Level.DEBUG, Level.TRACE}));
    }

    private void resetInitial() {
        initialConfig = serializer.doCopy(config);
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
        config.setLogPattern(patternTextField.getText());
        logMonitorSettignsDao.save(config);
        scheduler.reload();
    }

    @Override
    public void reset() {
        resetInitial();
        settingsModel = new ArrayListModel<ServerSettings>(config.getServerSettings());
        patternTextField.setText(config.getLogPattern());
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

    private void refreshMatdhers() {
        ServerSettings environment = getSelectedEnvironment();

        mathcerList.setModel(new ArrayListModel<MatchConfig>(environment == null ? new ArrayList<MatchConfig>() : environment.getMatchConfigs()));
    }

    private class RemoveListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            config.getServerSettings().remove(getSelectedEnvironment());
            refreshSettingsModel();
        }
    }

    private ServerSettings getSelectedEnvironment() {
        return (ServerSettings) environmentList.getSelectedValue();
    }

    private class ServerSelectionListener implements ListSelectionListener {
        @Override
        public void valueChanged(ListSelectionEvent e) {
            refreshMatdhers();
        }
    }

    private class AddPatternListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            getSelectedEnvironment().getMatchConfigs().add(createMatcher());
            mathcerNameTextField.setText("");
            refreshMatdhers();
        }

        private MatchConfig createMatcher() {
            MatchConfig matcher = new MatchConfig();
            matcher.setName(mathcerNameTextField.getText());
            matcher.setError(!ignoreCheckBox.isSelected());
            matcher.setLevel(levelComboBox.getSelectedItem().toString());
            //TODO validation
            return matcher;
        }
    }
}
