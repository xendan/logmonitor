package org.xendan.logmonitor.idea;

import org.xendan.logmonitor.dao.LogEntryDao;
import org.xendan.logmonitor.dao.LogMonitorSettingsDao;
import org.xendan.logmonitor.model.LogEntry;
import org.xendan.logmonitor.model.LogMonitorConfiguration;
import org.xendan.logmonitor.model.MatchConfig;
import org.xendan.logmonitor.model.ServerSettings;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeModel;

/**
 * User: id967161
 * Date: 10/09/13
 */
public class LogMonitorPanelModel {

    private final LogEntryDao logEntryDao;
    private final LogMonitorSettingsDao logMonitorSettingsDao;

    public LogMonitorPanelModel(LogEntryDao logEntryDao, LogMonitorSettingsDao logMonitorSettingsDao) {
        this.logEntryDao = logEntryDao;
        this.logMonitorSettingsDao = logMonitorSettingsDao;
    }

    public TreeModel getTreeModel() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode();
        for (LogMonitorConfiguration logMonitorConfiguration : logMonitorSettingsDao.getConfigs()) {
            root.add(createConfigNode(logMonitorConfiguration));
        }
        return new DefaultTreeModel(root);
    }

    private MutableTreeNode createConfigNode(LogMonitorConfiguration logMonitorConfiguration) {
        DefaultMutableTreeNode configNode  = new DefaultMutableTreeNode(logMonitorConfiguration.getProjectName());
        for (ServerSettings settings : logMonitorConfiguration.getServerSettings()) {
            configNode.add(createSettingsNode(settings));
        }
        return configNode;
    }

    private MutableTreeNode createSettingsNode(ServerSettings settings) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(settings.getName());
        for (MatchConfig matchConfig : settings.getMatchConfigs()) {
            node.add(createMatchNode(matchConfig));
        }
        return node;
    }

    private MutableTreeNode createMatchNode(MatchConfig matchConfig) {
        DefaultMutableTreeNode node =  new DefaultMutableTreeNode(matchConfig.getName());
        for (LogEntry entry : logEntryDao.getMatchedEntries(matchConfig)) {
            node.add(createEntryNode(entry));
        }
        return node;
    }

    private MutableTreeNode createEntryNode(LogEntry entry) {
        MutableTreeNode node = new DefaultMutableTreeNode(entry.getLevel() + ":" + entry.getMessage());
        return node;
    }
}
