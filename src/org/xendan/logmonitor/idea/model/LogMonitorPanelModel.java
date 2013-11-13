package org.xendan.logmonitor.idea.model;

import com.jgoodies.binding.value.ValueHolder;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.xendan.logmonitor.dao.ConfigurationDao;
import org.xendan.logmonitor.idea.BaseDialog;
import org.xendan.logmonitor.idea.MatchConfigForm;
import org.xendan.logmonitor.model.*;
import org.xendan.logmonitor.parser.EntryAddedListener;
import org.xendan.logmonitor.parser.PatternUtils;
import org.xendan.logmonitor.read.Serializer;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.event.ActionEvent;
import java.util.List;

/**
 * User: id967161
 * Date: 10/09/13
 */
public class LogMonitorPanelModel {

    public static final String LOADING = "Loading...";
    private final ConfigurationDao dao;
    private final Serializer serializer;
    private final EntryAddedListener listener;

    public LogMonitorPanelModel(ConfigurationDao dao, Serializer serializer, EntryAddedListener listener) {
        this.dao = dao;
        this.serializer = serializer;
        this.listener = listener;
    }

    public boolean hasConfig() {
        return !dao.getConfigs().isEmpty();
    }

    public DefaultTreeModel initTreeModel() {
        List<Configuration> configs = dao.getConfigs();
        if (configs.isEmpty()) {
            return null;
        }
        DefaultMutableTreeNode root = new DefaultMutableTreeNode();
        for (Configuration configuration : configs) {
            root.add(createConfigNode(configuration));
        }
        return new DefaultTreeModel(root);
    }

    private MutableTreeNode createConfigNode(Configuration configuration) {
        DefaultMutableTreeNode configNode = new DefaultMutableTreeNode(configuration);
        for (Environment environment : configuration.getEnvironments()) {
            configNode.add(createEnvironment(environment));
        }
        return configNode;
    }

    private MutableTreeNode createEnvironment(Environment environment) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(environment);
        for (MatchConfig matchConfig : environment.getMatchConfigs()) {
            node.add(createMatchNode(matchConfig, environment));
        }
        return node;
    }

    private MutableTreeNode createMatchNode(MatchConfig matchConfig, Environment environment) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(matchConfig);
        List<LogEntryGroup> groups = dao.getMatchedEntryGroups(matchConfig, environment);
        List<LogEntry> entries = dao.getNotGroupedMatchedEntries(matchConfig, environment);
        if (!groups.isEmpty() || !entries.isEmpty()) {
            return addGroupsAndEntries(node, groups, entries);
        }
        DefaultMutableTreeNode loading = new DefaultMutableTreeNode(LOADING);
        node.add(loading);
        return node;

    }

    private MutableTreeNode addGroupsAndEntries(DefaultMutableTreeNode node, List<LogEntryGroup> groups, List<LogEntry> entries) {
        for (LogEntryGroup group : groups) {
            node.add(createLogEntryGroupNode(group));
        }
        for (LogEntry entry : entries) {
            node.add(createEntryNode(entry));
        }
        return node;
    }

    private MutableTreeNode createLogEntryGroupNode(LogEntryGroup group) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(new GroupObject(group));
        for (LogEntry logEntry : group.getEntries()) {
            node.add(new DefaultMutableTreeNode(new GroupedEntryObject(logEntry, group)));
        }
        return node;
    }

    private MutableTreeNode createEntryNode(LogEntry entry) {
        return new DefaultMutableTreeNode(new EntryObject(entry));
    }

    public String getMessage(TreePath path) {
        if (path.getLastPathComponent() instanceof MutableTreeNode) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
            if (node.getUserObject() instanceof ConsoleDisplayable) {
                return ((ConsoleDisplayable) node.getUserObject()).toConsoleString();
            }
            if (node.getUserObject() instanceof Environment) {
                Environment environment = (Environment) node.getUserObject();
                return environment.getName() + ", " + getServerStr(environment.getServer());
            }
        }
        return path.getLastPathComponent().toString();
    }

    private String getServerStr(Server server) {
        if (server == null) {
            return Server.LOCALHOST;
        }
        return server.getLogin() + "@" + server.getHost();
    }


    private LogEntry getEntry(TreePath path) {
        if (path.getLastPathComponent() instanceof DefaultMutableTreeNode) {
            Object obj = ((DefaultMutableTreeNode) path.getLastPathComponent()).getUserObject();
            if (obj instanceof EntryObject) {
                return ((EntryObject) obj).getEntry();
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private <T> T getObjectFromPath(TreePath path, Class<T> objectClass) {
        if (path == null) {
            return null;
        }
        for (int i = 0; i < path.getPathCount(); i++) {
            Object component = path.getPathComponent(i);
            if (component instanceof DefaultMutableTreeNode) {
                Object obj = ((DefaultMutableTreeNode) component).getUserObject();
                if (obj != null && objectClass.equals(obj.getClass())) {
                    return (T) obj;
                }
            }
        }
        return null;
    }

    public void onEntriesAdded(Environment environment, DefaultTreeModel model) {
        DefaultMutableTreeNode envNode = findNode((DefaultMutableTreeNode) model.getRoot(), environment);
        for (MatchConfig matchConfig : environment.getMatchConfigs()) {
            DefaultMutableTreeNode node = findNode(envNode, matchConfig);
            while (node.getChildCount() != 0) {
                TreeNode child = node.getChildAt(0);
                model.removeNodeFromParent((MutableTreeNode) child);
            }
            for (LogEntryGroup group : dao.getMatchedEntryGroups(matchConfig, environment)) {
                model.insertNodeInto(createLogEntryGroupNode(group), node, node.getChildCount());
            }
            for (LogEntry entry : dao.getNotGroupedMatchedEntries(matchConfig, environment)) {
                model.insertNodeInto(createEntryNode(entry), node, node.getChildCount());
            }
        }
    }

    private DefaultMutableTreeNode findNode(DefaultMutableTreeNode node, Object userObject) {
        if (node.getUserObject() != null && node.getUserObject().equals(userObject)) {
            return node;
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            TreeNode child = node.getChildAt(i);
            DefaultMutableTreeNode otherNode = findNode((DefaultMutableTreeNode) child, userObject);
            if (otherNode != null) {
                return otherNode;
            }
        }
        return null;
    }

    public JPopupMenu getContextMenu(TreePath path, Runnable openConfigDialog) {
        JPopupMenu menu = new JPopupMenu();
        menu.add(new JMenuItem(new OpenConfig(openConfigDialog)));

        MatchConfig config = getObjectFromPath(path, MatchConfig.class);
        Configuration configuration = getObjectFromPath(path, Configuration.class);
        GroupObject group = getObjectFromPath(path, GroupObject.class);
        if (group != null) {
            menu.add(new JMenuItem(new CreateGroupedMatchConfig(configuration, group.getGroup(), config.getLevel())));
        }
        return menu;
    }

    private interface ConsoleDisplayable {
        String toConsoleString();
    }

    public static class EntryObject implements ConsoleDisplayable {
        private final LogEntry entry;

        public EntryObject(LogEntry entry) {
            this.entry = entry;
        }

        @Override
        public String toString() {
            return entry.getLevel() + ":" + entry.getDate() + StringUtils.abbreviate(entry.getMessage(), 10);
        }

        public LogEntry getEntry() {
            return entry;
        }

        @Override
        public String toConsoleString() {
            return entry.getLevel() + ":" + entry.getDate() + "\n" + getMessage();
        }

        protected String getMessage() {
            return entry.getMessage();
        }

        public boolean isError() {
            return Level.toLevel(entry.getLevel()).isGreaterOrEqual(Level.ERROR);
        }
    }

    private static class GroupedEntryObject extends EntryObject {

        private final LogEntryGroup group;

        public GroupedEntryObject(LogEntry entry, LogEntryGroup group) {
            super(entry);
            this.group = group;
        }

        @Override
        protected String getMessage() {
            return PatternUtils.regexToSimple(group.getMessagePattern());
        }
    }

    private static class GroupObject implements ConsoleDisplayable {
        private final LogEntryGroup group;

        public GroupObject(LogEntryGroup group) {
            this.group = group;
        }

        @Override
        public String toString() {
            return group.getEntries().size() + " similar entries";
        }

        @Override
        public String toConsoleString() {
            return toString() + " matched by \n" + group.getMessagePattern();
        }

        private LogEntryGroup getGroup() {
            return group;
        }
    }

    private class CreateGroupedMatchConfig extends AbstractAction {
        private final LogEntryGroup group;
        private final String level;
        private final Configuration configuration;

        public CreateGroupedMatchConfig(Configuration configuration, LogEntryGroup group, String level) {
            super("Create match...");
            this.configuration = configuration;
            this.group = group;
            this.level = level;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            MatchConfigForm matchConfigForm = new MatchConfigForm();
            final MatchConfig config = new MatchConfig();
            config.setMessage(group.getMessagePattern());
            config.setLevel(level);
            VerboseBeanAdapter<MatchConfig> beanAdapter = new VerboseBeanAdapter<MatchConfig>(config);
            matchConfigForm.setBeanAdapters(beanAdapter);
            final List<Environment> copy = serializer.doCopy(configuration.getEnvironments());
            matchConfigForm.setEnvironments(new ValueHolder(copy));
            matchConfigForm.setIsSpecific();
            BaseDialog dialog = new BaseDialog(new OnOkAction() {
                @Override
                public boolean doAction() {
                    doSaveSettings(config, copy, configuration.getEnvironments());
                    return true;
                }
            }, matchConfigForm.getContentPanel());
            dialog.setTitleAndShow("Add new match config");
        }

        private void doSaveSettings(MatchConfig config, List<Environment> copies, List<Environment> originals) {
            for (int i = 0; i < copies.size(); i++) {
                 if (copies.get(i).getMatchConfigs().contains(config)) {
                     dao.addMatchConfig(originals.get(i), config);
                     listener.onEntriesAdded(originals.get(i));
                 }
            }
        }
    }

    private class OpenConfig extends AbstractAction {
        private final Runnable openConfigDialog;

        private OpenConfig(Runnable openConfigDialog) {
            super("Open config");
            this.openConfigDialog = openConfigDialog;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            openConfigDialog.run();
        }
    }
}
