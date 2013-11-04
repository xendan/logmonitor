package org.xendan.logmonitor.idea;

import org.apache.log4j.Level;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.xendan.logmonitor.dao.ConfigurationDao;
import org.xendan.logmonitor.idea.model.LogMonitorPanelModel;
import org.xendan.logmonitor.model.*;

import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import java.util.Arrays;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * User: id967161
 * Date: 20/09/13
 */
public class LogMonitorPanelModelTest {

    private LogMonitorPanelModel model;
    private ConfigurationDao dao;
    private LogEntryGroup group;
    private LogEntry singleEntry;
    private LogEntry groupedEntry;

    @Test
    public void test_rebuildTreeModel() throws Exception {
        TreeModel treeModel = model.initTreeModel();
        assertEquals("Expect config only for one project", 1, treeModel.getChildCount(treeModel.getRoot()));
        Object configNode = treeModel.getChild(treeModel.getRoot(), 0);
        assertEquals("Expect 1 environment", 1, treeModel.getChildCount(configNode));
        Object matchNode = treeModel.getChild(configNode, 0);
        assertEquals("Expect 1 match config", 1, treeModel.getChildCount(matchNode));
        Object envNode = treeModel.getChild(matchNode, 0);
        assertEquals("Expect 2 node for grouped entries and for single entries ", 2, treeModel.getChildCount(envNode));
        Object groupNode = treeModel.getChild(envNode, 0);
        assertEquals("Expect 1 node for grouped entries", 1 , treeModel.getChildCount(groupNode));
    }

    @Test
    public void test_getMessage() throws Exception {
        TreeModel treeModel = model.initTreeModel();
        Object configNode = treeModel.getChild(treeModel.getRoot(), 0);
        Object matchNode = treeModel.getChild(configNode, 0);
        Object envNode = treeModel.getChild(matchNode, 0);
        Object groupNode = treeModel.getChild(envNode, 0);
        TreePath path = new TreePath(new Object[]{treeModel.getRoot(), configNode, matchNode, envNode, groupNode});
        assertEquals("1 similar entries matched by \nS\\[S\\]S\\\\", model.getMessage(path));
        Object groupedEntryNode = treeModel.getChild(groupNode, 0);
        path = new TreePath(new Object[]{treeModel.getRoot(), configNode, matchNode, envNode, groupNode, groupedEntryNode});
        assertEquals("ERROR:1970-01-01T01:00:00.000\nS[S]S\\", model.getMessage(path));
    }

    @Before
    public void setUp() {
        dao = mock(ConfigurationDao.class);
        model = new LogMonitorPanelModel(dao);

        Configuration config = new Configuration();
        Environment environment = new Environment();
        MatchConfig matchConfig = new MatchConfig();
        environment.getMatchConfigs().add(matchConfig);
        config.getEnvironments().add(environment);
        when(dao.getConfigs()).thenReturn(Arrays.asList(config));
        singleEntry = new LogEntry();
        List<LogEntry> notGrouped = Arrays.asList(singleEntry);
        when(dao.getNotGroupedMatchedEntries(matchConfig, environment)).thenReturn(notGrouped);
        group = new LogEntryGroup();
        group.setMessagePattern("S\\[S\\]S\\\\");
        groupedEntry = new LogEntry();
        groupedEntry.setLevel(Level.ERROR.toString());
        groupedEntry.setDate(new LocalDateTime(0));
        group.getEntries().add(groupedEntry);
        List<LogEntryGroup> entryGroups = Arrays.asList(group);
        when(dao.getMatchedEntryGroups(matchConfig, environment)).thenReturn(entryGroups);
    }
}
