package org.xendan.logmonitor.idea;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import com.intellij.ui.ListCellRendererWrapper;
import com.jgoodies.binding.adapter.Bindings;
import com.jgoodies.binding.adapter.SpinnerAdapterFactory;
import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.binding.value.AbstractConverter;
import com.jgoodies.binding.value.ValueHolder;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.common.collect.ArrayListModel;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.xendan.logmonitor.dao.LogMonitorSettingsDao;
import org.xendan.logmonitor.model.Configuration;
import org.xendan.logmonitor.model.Environment;
import org.xendan.logmonitor.model.MatchConfig;
import org.xendan.logmonitor.model.Server;
import org.xendan.logmonitor.read.ReaderScheduler;
import org.xendan.logmonitor.read.Serializer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * User: id967161
 * Date: 03/09/13
 */
public class LogMonitorSettingsConfigurable implements SearchableConfigurable, Configurable.NoScroll {
    public static final String ENVIRONMENTS = "environments";
    private final Serializer serializer;
    private final LogMonitorSettingsDao logMonitorSettignsDao;
    private final ReaderScheduler scheduler;
    private final Project project;
    final EnvironmentsModel environmentsModel;
    final MatchConfigModel matchConfigModel;
    private final ArrayListModel<Configuration> configsModel;
    private List<Configuration> initialConfigs;
    private JPanel contentPanel;
    private JButton removeLogSettingsButton;

    JTextField pathTextField;
    private JButton addLogSettingsButton;
    private JButton addPatternButton;
    private JButton removePatternButton;
    private JScrollPane patternsListPanel;
    private JTextField patternTextField;
    JComboBox projectComboBox;
    private JLabel projectLabel;
    JList logSettingsList;
    JComboBox serverComboBox;
    JPanel serverPanel;
    JTextField serverHostTextField;
    JTextField serverLoginTextField;
    JPasswordField serverPasswordField;
    JTextField keyFileTextField;
    JButton selectKeyFileButton;
    private JLabel serverLabel;
    private JLabel pathLabel;
    private JButton patternUp;
    private JButton patternDown;
    JTextField logSettingsNametextField;
    JButton addProjectButton;
    JTextField projectNameTextField;
    private JLabel logSettingsLabel;
    private JPanel serverPanelBig;
    private JSpinner updateIntervalSpinner;
    private JButton browsLogButton;
    private JPasswordField serverPastPhrsePasswordField;
    private JLabel updateIntrevalLabel;
    JList paternsList;
    private JPanel matchConfigPanel;
    private JPanel logSettingsPanel;
    JButton projectRemoveButton;
    private static final Server ADD_NEW = new Server("Add new...", -2);
    public static final Server LOCALHOST = new Server("localhost", -1);
    final VerboseBeanAdapter<Configuration> configAdapter;

    public LogMonitorSettingsConfigurable(Project project) {
        this(project,
                ServiceManager.getService(LogMonitorSettingsDao.class),
                ServiceManager.getService(Serializer.class),
                ServiceManager.getService(ReaderScheduler.class));
    }

    public LogMonitorSettingsConfigurable(Project project, LogMonitorSettingsDao logMonitorSettingsDao, Serializer serializer, ReaderScheduler readerScheduler) {
        this.logMonitorSettignsDao = logMonitorSettingsDao;
        this.serializer = serializer;
        this.scheduler = readerScheduler;
        this.project = project;

        this.configAdapter = new VerboseBeanAdapter<Configuration>(new Configuration());
        configsModel = new ArrayListModel<Configuration>(logMonitorSettignsDao.getConfigs());
        environmentsModel = new EnvironmentsModel();
        matchConfigModel = new MatchConfigModel();
        init();

    }

    private void init() {
        addProjectButton.addActionListener(new AddProjectActionListener());
        projectRemoveButton.addActionListener(new RemoveProjectListener());
        browsLogButton.addActionListener(new BrowseLogButtonActionListener());
        selectKeyFileButton.addActionListener(new KeyFileSelectionListener());
        projectComboBox.setRenderer(new ConfigProjectRenderer());
        ValueHolder configSelection = new ValueHolder();
        Bindings.bind(projectComboBox, new SelectionInList<Configuration>((ListModel) configsModel, configSelection));
        Bindings.bind(patternTextField, configAdapter.getPropertyModel("logPattern"));
        configSelection.addValueChangeListener(new ConfigChangeListener());

        List<Configuration> configs = logMonitorSettignsDao.getConfigs();
        initialConfigs = new ArrayList<Configuration>();
        Configuration configForProject = findConfigForProject(configs);
        if (configForProject == null) {
            configForProject = new Configuration();
            configForProject.setProjectName(project.getName());
            initialConfigs.add(configForProject);
        }
        initialConfigs.addAll(configs);
    }

    @NotNull
    @Override
    public String getId() {
        return "logmonitor.settings";
    }


    private void resetInitial() {
        initialConfigs = serializer.doCopy(new ArrayList<Configuration>(configsModel));
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
        return !initialConfigs.equals(new ArrayList<Configuration>(configsModel));
    }

    @Override
    public void apply() throws ConfigurationException {
        environmentsModel.onItemCommit();
        matchConfigModel.onItemCommit();
        resetInitial();
        logMonitorSettignsDao.save(configsModel);
        scheduler.reload();
    }


    @Override
    public void reset() {
        configsModel.clear();
        configsModel.addAll(serializer.doCopy(initialConfigs));
        projectComboBox.setSelectedItem(findConfigForProject());
        setProjectButtonCreate();
        projectNameTextField.setText("");
        environmentsModel.disableItemPanel();
        matchConfigModel.disableItemPanel();
        setMatchConfigEnabled(false);
    }

    private Configuration findConfigForProject() {
        for (Configuration config : configsModel) {
            if (project.getName().equals(config.getProjectName())) {
                return config;
            }

        }
        return null;
    }

    private void setMatchConfigEnabled(boolean enabled) {
        addPatternButton.setEnabled(enabled);
        removePatternButton.setEnabled(enabled);
        paternsList.setEnabled(enabled);
        patternUp.setEnabled(enabled);
        patternDown.setEnabled(enabled);
    }


    private Configuration findConfigForProject(List<Configuration> configs) {
        for (Configuration config : configs) {
            if (config.getProjectName().equals(project.getName())) {
               return config;
            }
        }
        return null;
    }

    @Override
    public void disposeUIResources() {
    }

    private void setProjectButtonCreate() {
        projectNameTextField.setVisible(false);
        addProjectButton.setText("New project");
    }


    private class AddProjectActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (projectNameTextField.isVisible()) {
                Configuration config = new Configuration();
                config.setProjectName(projectNameTextField.getText());
                configsModel.add(config);
                projectComboBox.setSelectedItem(config);
                setProjectButtonCreate();
            } else {
                projectNameTextField.setVisible(true);
                addProjectButton.setText("Add project");
            }
        }
    }

    private class BrowseLogButtonActionListener implements ActionListener, LogChooseListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            Server server = environmentsModel.getSelected().getServer();
            if (isLocalHost(server)) {
                File file = selectFile();
                if (file != null) {
                    pathTextField.setText(file.getAbsolutePath());
                }
            } else {
                ServerLogChooser chooser = new ServerLogChooser(server, this);
                chooser.setSize(300, 300);
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

    private File selectFile() {
        final JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int returnVal = fc.showOpenDialog(contentPanel);
        boolean selected = returnVal == JFileChooser.APPROVE_OPTION;
        return selected ? fc.getSelectedFile() : null;
    }

    class EnvironmentsModel extends SetItemFromListModel<Environment> {
        public static final String GENERAL_ERROR = "General Error";
        public static final int DEFAULT_TIME_MIN = 10;
        private ValueModel servers;
        private VerboseBeanAdapter<Server> serverAdapter = new VerboseBeanAdapter<Server>(new Server());

        public EnvironmentsModel() {
            super(addLogSettingsButton, removeLogSettingsButton, logSettingsPanel, logSettingsList, configAdapter.getPropertyModel(ENVIRONMENTS), "name");
        }


        @Override
        protected void bind(VerboseBeanAdapter<Environment> beanAdapter) {
            servers = new ValueHolder(getServers(), true);
            serverAdapter = new VerboseBeanAdapter<Server>(new Server());
            Bindings.bind(logSettingsNametextField, beanAdapter.getPropertyModel("name"));
            Bindings.bind(pathTextField, beanAdapter.getPropertyModel("path"));
            ValueModel serverPropertyModel = beanAdapter.getPropertyModel("server");
            serverPropertyModel.addValueChangeListener(new IsSeverNullListener());
            Bindings.bind(serverComboBox, new SelectionInList<Server>(servers, new ServerInListModel(serverPropertyModel, serverAdapter)));
            Bindings.bind(serverHostTextField, serverAdapter.getPropertyModel("host"));
            Bindings.bind(serverLoginTextField, serverAdapter.getPropertyModel("login"));
            Bindings.bind(serverPasswordField, serverAdapter.getPropertyModel("password"));
            Bindings.bind(keyFileTextField, serverAdapter.getPropertyModel("keyPath"));
            Bindings.bind(serverPastPhrsePasswordField, serverAdapter.getPropertyModel("passPhrase"));

            ValueModel   levelModel   = beanAdapter.getPropertyModel("updateInterval");
            SpinnerModel spinnerModel = new SpinnerNumberModel(DEFAULT_TIME_MIN, 1, 1000, 1);
            SpinnerAdapterFactory.connect(spinnerModel, levelModel, DEFAULT_TIME_MIN);
            updateIntervalSpinner.setModel(spinnerModel);
        }

        @Override
        protected void onNewClicked() {
            super.onNewClicked();
            servers.setValue(getServers());
            serverComboBox.setSelectedItem(ADD_NEW);
        }

        @Override
        protected Environment initBean(Environment environment) {
            boolean addDefault = true;
            for (Environment other : configAdapter.getBean().getEnvironments()) {
                if (other != environment) {
                    for (MatchConfig matchConfig : other.getMatchConfigs()) {
                        if (!environment.getMatchConfigs().contains(matchConfig)) {
                            environment.getMatchConfigs().add(matchConfig);
                            if (Level.ERROR.toString().equals(matchConfig.getLevel())
                                    && StringUtils.isEmpty(matchConfig.getMessage())
                                    && GENERAL_ERROR.equals(matchConfig.getName())) {
                                addDefault = false;
                            }
                        }
                    }
                }
            }
            if (addDefault) {
                MatchConfig defConfig = new MatchConfig();
                defConfig.setName(GENERAL_ERROR);
                defConfig.setLevel(Level.ERROR.toString());
                defConfig.setUseArchive(true);
                defConfig.setShowNotification(true);
                environment.getMatchConfigs().add(defConfig);
            }
            environment.setUpdateInterval(DEFAULT_TIME_MIN);
            return environment;
        }

        @Override
        protected void onItemSet() {
            servers.setValue(getServers());
            setMatchConfigEnabled(true);
        }

        @Override
        protected void onItemCommit() {
            super.onItemCommit();
            for (int i = 0; i < logSettingsList.getModel().getSize(); i++) {
                 Environment settings = (Environment) logSettingsList.getModel().getElementAt(i);
                 Server server = settings.getServer();
                 if (isLocalHost(server)) {
                       settings.setServer(null);
                 }

            }

        }

        @Override
        protected boolean isInvalid(Environment item) {
            return StringUtils.isEmpty(item.getName())
                    || StringUtils.isEmpty(item.getPath());
        }

        private List<Server> getServers() {
            List<Server> servers = new ArrayList<Server>();
            servers.add(ADD_NEW);
            servers.add(LOCALHOST);
            for (Configuration config : configsModel) {
                for (Environment environment : config.getEnvironments()) {
                    if (environment.getServer() != null) {
                        servers.add(environment.getServer());
                    }
                }
            }
            return servers;
        }


        private class IsSeverNullListener implements PropertyChangeListener {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                setPanelEnabled(serverPanel, evt.getNewValue() != null);
            }
        }
    }

    private boolean isLocalHost(Server server) {
        return server == null || StringUtils.isEmpty(server.getHost()) ||
                LOCALHOST.getHost().equals(server.getHost()) || "127.0.0.1".equals(server.getHost());
    }


    private class ServerInListModel extends AbstractConverter implements PropertyChangeListener {

        private final ValueModel subject;
        private final VerboseBeanAdapter<Server> serverAdapter;

        /**
         * Constructs an AbstractConverter on the given subject.
         *
         * @param subject the ValueModel that holds the source value
         * @throws NullPointerException if the subject is {@code null}
         */
        public ServerInListModel(ValueModel subject, VerboseBeanAdapter<Server> serverAdapter) {
            super(subject);
            this.subject = subject;
            this.serverAdapter = serverAdapter;
            subject.addValueChangeListener(this);
        }

        @Override
        public Object convertFromSubject(Object subjectValue) {
            if (subjectValue == null) {
                return LOCALHOST;
            }
            return subjectValue;
        }

        @Override
        public void setValue(Object newValue) {
            Server newServer = getNewValue(newValue);
            serverAdapter.setBean(newServer);
            subject.setValue(newServer);
        }

        private Server getNewValue(Object newValue) {
            if (newValue == LOCALHOST) {
                return null;
            } else if (newValue == ADD_NEW) {
                return new Server();

            } else {
                return (Server) newValue;
            }
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            serverAdapter.setBean((Server) evt.getNewValue());
            firePropertyChange(evt.getPropertyName(), evt.getOldValue(), convertFromSubject(evt.getNewValue()));
        }
    }

    private class ConfigChangeListener implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            configAdapter.setBean((Configuration) evt.getNewValue());
        }
    }

    private class ConfigProjectRenderer extends ListCellRendererWrapper {


        @Override
        public void customize(JList jList, Object o, int i, boolean b, boolean b2) {
            if (o instanceof Configuration) {
                setText(((Configuration) o).getProjectName());
            }
        }
    }


    private class MatchConfigModel extends  SetItemFromListModel<MatchConfig> {
        private MatchConfigForm form;

        public MatchConfigModel() {
            super(addPatternButton, removePatternButton, matchConfigPanel, paternsList, environmentsModel.getBeanModel("matchConfigs"), "name");
        }

        @Override
        protected void bind(VerboseBeanAdapter<MatchConfig> beanAdapter) {
            form = new MatchConfigForm();
            matchConfigPanel.setLayout(new BoxLayout(matchConfigPanel, BoxLayout.PAGE_AXIS));
            matchConfigPanel.add(form.contentPanel);
            form.setBeanAdapter(beanAdapter);
            form.setLogSettingsList(configAdapter.getPropertyModel(ENVIRONMENTS));
            setPanelEnabled(itemPanel, false);
            form.hideException();
        }

        @Override
        protected void onNewClicked() {
            super.onNewClicked();
            form.setIsArchive(true);
            form.setShowNotification(true);
        }

        @Override
        protected boolean isInvalid(MatchConfig item) {
            return StringUtils.isEmpty(item.getName());
        }
    }

    private class RemoveProjectListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            configsModel.remove(projectComboBox.getSelectedItem());
        }
    }

    private class KeyFileSelectionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            File file = selectFile();
            if (file != null) {
                keyFileTextField.setText(file.getAbsolutePath());
            }
        }
    }
}
