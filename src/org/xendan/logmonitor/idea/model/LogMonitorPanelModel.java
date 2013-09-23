package org.xendan.logmonitor.idea.model;

import org.xendan.logmonitor.model.Configuration;
import org.xendan.logmonitor.model.Environment;
import org.xendan.logmonitor.model.LogEntry;
import org.xendan.logmonitor.model.MatchConfig;
import org.xendan.logmonitor.service.LogService;

import javax.swing.tree.*;
import java.util.List;

/**
 * User: id967161
 * Date: 10/09/13
 */
public class LogMonitorPanelModel {

    private final LogService service;

    public LogMonitorPanelModel(LogService service) {
        this.service = service;
    }

    public TreeModel rebuildTreeModel() {
        List<Configuration> configs = service.getConfigs();
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

    private MutableTreeNode createSettingsNode(Environment settings) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(settings);
        for (MatchConfig matchConfig : settings.getMatchConfigs()) {
            node.add(createMatchNode(matchConfig));
        }
        return node;
    }

    private MutableTreeNode createMatchNode(MatchConfig matchConfig) {
        DefaultMutableTreeNode node =  new DefaultMutableTreeNode(matchConfig.getName());
        for (LogEntry entry : service.getMatchedEntries(matchConfig)) {
            node.add(createEntryNode(entry));
        }
        return node;
    }

    private MutableTreeNode createEntryNode(LogEntry entry) {
        return new DefaultMutableTreeNode(new EntryObject(entry));
    }

    public String getMessage(TreePath path) {
        if (path.getLastPathComponent() instanceof MutableTreeNode) {
            MutableTreeNode node = (MutableTreeNode) path.getLastPathComponent();
            return node.toString();
        }
        return null;
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
            service.addMatchConfig(matcher, entry.getMatchConfig(), settings);
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
            service.clearEntries(settings);
        }
    }


    private class EntryObject {
        private final LogEntry entry;

        public EntryObject(LogEntry entry) {
            this.entry = entry;
        }

        @Override
        public String toString() {
            return entry.getLevel() + ":" + entry.getMessage();
        }

        public LogEntry getEntry() {
            return entry;
        }
    }
}
