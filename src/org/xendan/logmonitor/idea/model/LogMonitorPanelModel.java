package org.xendan.logmonitor.idea.model;

import org.apache.commons.lang.StringUtils;
import org.xendan.logmonitor.dao.ConfigurationDao;
import org.xendan.logmonitor.model.*;
import org.xendan.logmonitor.parser.PatternUtils;

import javax.swing.tree.*;
import java.util.List;

/**
 * User: id967161
 * Date: 10/09/13
 */
public class LogMonitorPanelModel {

    private final ConfigurationDao dao;

    public LogMonitorPanelModel(ConfigurationDao dao) {
        this.dao = dao;
    }

    public boolean hasConfig() {
        return !dao.getConfigs().isEmpty();
    }

    public TreeModel rebuildTreeModel() {
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
        DefaultMutableTreeNode configNode  = new DefaultMutableTreeNode(configuration.getProjectName());
        for (Environment settings : configuration.getEnvironments()) {
            configNode.add(createSettingsNode(settings));
        }
        return configNode;
    }

    private MutableTreeNode createSettingsNode(Environment environment) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(environment);
        for (MatchConfig matchConfig : environment.getMatchConfigs()) {
            node.add(createMatchNode(matchConfig, environment));
        }
        return node;
    }

    private MutableTreeNode createMatchNode(MatchConfig matchConfig, Environment environment) {
        DefaultMutableTreeNode node =  new DefaultMutableTreeNode(matchConfig.getName());
        for (LogEntryGroup group : dao.getMatchedEntryGroups(matchConfig, environment)) {
            node.add(createLogEntryNode(group));
        }
        for (LogEntry entry : dao.getNotGroupedMatchedEntries(matchConfig, environment)) {
            node.add(createEntryNode(entry));
        }
        return node;
    }

    private MutableTreeNode createLogEntryNode(LogEntryGroup group) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(new EntryGroupObject(group));
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
                return ((ConsoleDisplayable)node.getUserObject()).toConsoleString();
            }
        }
        return path.getLastPathComponent().toString();
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

    public void addMatchConfig(MatchConfig matcher, TreePath treePath) {
        LogEntry entry = getEntry(treePath);
        Environment settings = getSettings(treePath);
        if (entry != null) {
            dao.addMatchConfig(matcher, entry.getMatchConfig(), settings);
        }
    }

    private Environment getSettings(TreePath path) {
        if (path == null) {
            return null;
        }
        for (int i = 0; i < path.getPathCount(); i++) {
            Object component = path.getPathComponent(i);
            if (component instanceof DefaultMutableTreeNode) {
                Object obj = ((DefaultMutableTreeNode) component).getUserObject();
                if (obj instanceof Environment) {
                    return (Environment) obj;
                }
            }

        }

        return null;
    }

    public void clearAll(TreePath selectedPath) {
        Environment settings = getSettings(selectedPath);
        if (settings != null) {
            dao.clearEntries(settings);
        }
    }

    private interface ConsoleDisplayable {
        String toConsoleString();
    }

    private static class EntryObject implements ConsoleDisplayable {
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

    private static class EntryGroupObject implements ConsoleDisplayable {
        private final LogEntryGroup group;

        public EntryGroupObject(LogEntryGroup group) {
            this.group = group;
        }

        @Override
        public String toString() {
            return group.getEntries().size() + " similar entries";
        }

        @Override
        public String toConsoleString() {
            return toString() + " matched by \n"+ group.getMessagePattern();
        }
    }
}
