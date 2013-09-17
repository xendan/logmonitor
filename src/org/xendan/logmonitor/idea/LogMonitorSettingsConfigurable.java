package org.xendan.logmonitor.idea;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import com.jgoodies.binding.adapter.Bindings;
import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.binding.value.AbstractConverter;
import com.jgoodies.binding.value.ValueHolder;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.common.collect.ArrayListModel;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.xendan.logmonitor.dao.LogMonitorSettingsDao;
import org.xendan.logmonitor.model.LogMonitorConfiguration;
import org.xendan.logmonitor.model.LogSettings;
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
    private final Serializer serializer;
    private final LogMonitorSettingsDao logMonitorSettignsDao;
    private final ReaderScheduler scheduler;
    private final Project project;
    final LogSettingsModel logSettingsModel;
    private final ArrayListModel<LogMonitorConfiguration> configsModel;
    private List<LogMonitorConfiguration> initialConfigs;
    private JPanel contentPanel;
    private JButton removeLogSettingsButton;

    JTextField pathTextField;
    private JButton addLogSettingsButton;
    private JButton addPatternButton;
    private JButton removePatternButton;
    private JComboBox levelComboBox;
    private JScrollPane patternsListPanel;
    private JTextField mathcerNameTextField;
    private JTextField patternTextField;
    JComboBox projectComboBox;
    private JLabel projectLabel;
    private JTextField matchMessageTextField;
    private JCheckBox userArchiveCheckBox;
    private JPasswordField passwordField;
    private JTextArea messageTextArea;
    JList logSettingsList;
    JComboBox serverComboBox;
    private JPanel serverPanel;
    private JButton saveLogSettingsButton;
    JTextField serverHostTextField;
    JTextField serverLoginTextField;
    JPasswordField serverPasswordField;
    JTextField keyFiletextField;
    JButton selectFileButton;
    private JLabel serverLabel;
    private JLabel pathLabel;
    private JButton patternUp;
    private JButton patternDown;
    JTextField logSettingsNametextField;
    private JButton addProjectButton;
    private JTextField projectNameTextField;
    private JLabel logSettingsLabel;
    private JPanel serverPanelBig;
    private JSpinner updateIntervalSpinner;
    private JButton broswsLogButton;
    private JPasswordField serverPastPhrsePasswordField;
    private JLabel updateIntrevalLabel;
    private JList paternsList;
    private JPanel matchConfigPanel;
    private JPanel logSettingsPanel;
    private static final Server ADD_NEW = new Server("Add new...", -2);
    public static final Server LOCALHOST = new Server("localhost", -1);
    final VerboseBeanAdapter<LogMonitorConfiguration> configAdapter;

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

        this.configAdapter = new VerboseBeanAdapter<LogMonitorConfiguration>(new LogMonitorConfiguration());
        configsModel = new ArrayListModel<LogMonitorConfiguration>(logMonitorSettignsDao.getConfigs());
        logSettingsModel = new LogSettingsModel();
        init();

    }

    private void init() {
        addProjectButton.addActionListener(new AddProjectActionListener());
        broswsLogButton.addActionListener(new BrowseLogButtonActionListener());
        ValueHolder configSelection = new ValueHolder();
        Bindings.bind(projectComboBox, new SelectionInList<LogMonitorConfiguration>((ListModel) configsModel, configSelection));
        configSelection.addValueChangeListener(new ConfigChangeListener());
    }

    @NotNull
    @Override
    public String getId() {
        return "logmonitor.settings";
    }


    private void resetInitial() {
        initialConfigs = serializer.doCopy(new ArrayList<LogMonitorConfiguration>(configsModel));
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
        return !initialConfigs.equals(new ArrayList<LogMonitorConfiguration>(configsModel));
    }

    @Override
    public void apply() throws ConfigurationException {
        resetInitial();
        logMonitorSettignsDao.save(configsModel);
        scheduler.reload();
    }

    private LogMonitorConfiguration selectedConfig() {
        return configAdapter.getBean();
    }

    @Override
    public void reset() {
        resetInitial();
        refresh();
    }

    protected void refresh() {
        patternTextField.setText(selectedConfig().getLogPattern());
        List<LogMonitorConfiguration> configs = logMonitorSettignsDao.getConfigs();
        configsModel.clear();
        LogMonitorConfiguration configForProject = findConfigForProject(configs);
        if (configForProject == null) {
            configForProject = new LogMonitorConfiguration();
            configForProject.setProjectName(project.getName());
            configsModel.add(configForProject);
        }
        configsModel.addAll(configs);
        projectComboBox.setSelectedItem(configForProject);
        setProjectButtonCreate();
        projectNameTextField.setText("");
    }

    private LogMonitorConfiguration findConfigForProject(List<LogMonitorConfiguration> configs) {
        for (LogMonitorConfiguration config : configs) {
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
        addProjectButton.setText("Create new");
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
            Server server = logSettingsModel.getSelected().getServer();
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

    class LogSettingsModel extends SetItemFromListModel<LogSettings> {
        private ValueModel servers;
        private VerboseBeanAdapter<Server> serverAdapter = new VerboseBeanAdapter<Server>(new Server());

        public LogSettingsModel() {
            super(addLogSettingsButton, removeLogSettingsButton, logSettingsPanel, logSettingsList, configAdapter.getPropertyModel("logSettings"));
        }


        @Override
        protected void bind(VerboseBeanAdapter<LogSettings> beanAdapter) {
            servers = new ValueHolder(getServers(), true);
            serverAdapter = new VerboseBeanAdapter<Server>(new Server());
            Bindings.bind(logSettingsNametextField, beanAdapter.getPropertyModel("name"));
            Bindings.bind(pathTextField, beanAdapter.getPropertyModel("path"));
            Bindings.bind(serverComboBox, new SelectionInList<Server>(servers, new ServerInListModel(beanAdapter.getPropertyModel("server"), serverAdapter)));
            Bindings.bind(serverHostTextField, serverAdapter.getPropertyModel("host"));
            Bindings.bind(serverLoginTextField, serverAdapter.getPropertyModel("login"));
            Bindings.bind(serverPasswordField, serverAdapter.getPropertyModel("password"));
            Bindings.bind(keyFiletextField, serverAdapter.getPropertyModel("keyPath"));
            Bindings.bind(serverPastPhrsePasswordField, serverAdapter.getPropertyModel("passPhrase"));
        }

        @Override
        protected void onNewClicked() {
            super.onNewClicked();
            servers.setValue(getServers());
            serverComboBox.setSelectedItem(ADD_NEW);
        }

        @Override
        protected void onItemSet() {
            servers.setValue(getServers());
        }

        @Override
        protected boolean isInvalid(LogSettings item) {
            return StringUtils.isEmpty(item.getName())
                    || StringUtils.isEmpty(item.getPath());
        }


    }

    private List<Server> getServers() {
        List<Server> servers = new ArrayList<Server>();
        servers.add(ADD_NEW);
        servers.add(LOCALHOST);
        for (LogMonitorConfiguration config : configsModel) {
            for (LogSettings logSettings : config.getLogSettings()) {
                if (logSettings.getServer() != null) {
                    servers.add(logSettings.getServer());
                }
            }
        }
        return servers;
    }

    private class ServerInListModel extends AbstractConverter {

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
    }

    private class ConfigChangeListener implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            configAdapter.setBean((LogMonitorConfiguration) evt.getNewValue());
        }
    }
}
