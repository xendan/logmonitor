package org.xendan.logmonitor.idea;

import org.apache.log4j.Level;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.xendan.logmonitor.dao.*;
import org.xendan.logmonitor.idea.model.LogMonitorPanelModel;
import org.xendan.logmonitor.model.*;
import org.xendan.logmonitor.parser.EntryAddedListener;
import org.xendan.logmonitor.read.Serializer;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.util.Arrays;
import java.util.List;

import static junit.framework.TestCase.*;
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
    private Environment environment;
    private MatchConfig matchConfig;

    @Test
    public void test_isNodeUpdated() throws Exception {
        model.initTreeModel(new DefaultCallBack<DefaultTreeModel>() {
            @Override
            public void onAnswer(DefaultTreeModel treeModel) {
                long now = System.currentTimeMillis();

                group.getEntries().add(createEntry(now + 500000));
                when(dao.getMatchedEntryGroups(matchConfig, environment))
                        .thenReturn(Arrays.asList(group));
                model.onEntriesAdded(new LocalDateTime(now), environment, (DefaultTreeModel) treeModel);
                Object root = treeModel.getRoot();
                assertTrue("Root should be updated", model.isNodeUpdated((DefaultMutableTreeNode) root));
                DefaultMutableTreeNode configNode = (DefaultMutableTreeNode) treeModel.getChild(root, 0);
                assertTrue("Config should be updated", model.isNodeUpdated(configNode));
                DefaultMutableTreeNode matchNode = (DefaultMutableTreeNode) treeModel.getChild(configNode, 0);
                assertTrue("Match should be updated", model.isNodeUpdated(matchNode));
                DefaultMutableTreeNode envNode = (DefaultMutableTreeNode) treeModel.getChild(matchNode, 0);
                assertTrue("Environment should be updated", model.isNodeUpdated(envNode));
                DefaultMutableTreeNode groupNode = (DefaultMutableTreeNode) treeModel.getChild(envNode, 0);
                assertTrue("groupNode should be updated", model.isNodeUpdated(groupNode));
                DefaultMutableTreeNode firstGrouped = (DefaultMutableTreeNode) treeModel.getChild(groupNode, 0);
                assertFalse("First grouped not updated", model.isNodeUpdated(firstGrouped));
                DefaultMutableTreeNode secondGrouped = (DefaultMutableTreeNode) treeModel.getChild(groupNode, 1);
                assertTrue("Second grouped updated", model.isNodeUpdated(secondGrouped));
                DefaultMutableTreeNode notGrouped = (DefaultMutableTreeNode) treeModel.getChild(envNode, 1);
                assertFalse("Expect not group not updated", model.isNodeUpdated(notGrouped));
            }
        });

    }



    @Test
    public void test_rebuildTreeModel() throws Exception {
        model.initTreeModel(new DefaultCallBack<DefaultTreeModel>() {
            @Override
            public void onAnswer(DefaultTreeModel treeModel) {
                assertEquals("Expect config only for one project", 1, treeModel.getChildCount(treeModel.getRoot()));
                Object configNode = treeModel.getChild(treeModel.getRoot(), 0);
                assertEquals("Expect 1 environment", 1, treeModel.getChildCount(configNode));
                Object matchNode = treeModel.getChild(configNode, 0);
                assertEquals("Expect 1 match config", 1, treeModel.getChildCount(matchNode));
                Object envNode = treeModel.getChild(matchNode, 0);
                assertEquals("Expect 2 node for grouped entries and for single entries ", 2, treeModel.getChildCount(envNode));
                Object groupNode = treeModel.getChild(envNode, 0);
                assertEquals("Expect 1 node for grouped entries", 1, treeModel.getChildCount(groupNode));
            }
        });

    }

    @Test
    public void test_getMessage() throws Exception {
        model.initTreeModel(new DefaultCallBack<DefaultTreeModel>() {
            @Override
            public void onAnswer(DefaultTreeModel treeModel) {
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
        });
    }

    @Before
    public void setUp() {
        dao = mock(ConfigurationDao.class);
        //nulll
        model = new LogMonitorPanelModel(null, new LogService(null), mock(Serializer.class), mock(EntryAddedListener.class), null);

        Configuration config = new Configuration();
        environment = new Environment();
        matchConfig = new MatchConfig();
        environment.getMatchConfigs().add(matchConfig);
        config.getEnvironments().add(environment);
        when(dao.getConfigs()).thenReturn(Arrays.asList(config));
        LogEntry singleEntry = createEntry(5000);
        List<LogEntry> notGrouped = Arrays.asList(singleEntry);
        when(dao.getNotGroupedMatchedEntries(matchConfig, environment)).thenReturn(notGrouped);
        group = new LogEntryGroup();
        group.setMessagePattern("S\\[S\\]S\\\\");
        LogEntry groupedEntry = createEntry(0);

        groupedEntry.setLevel(Level.ERROR.toString());
        group.getEntries().add(groupedEntry);
        List<LogEntryGroup> entryGroups = Arrays.asList(group);
        when(dao.getMatchedEntryGroups(matchConfig, environment)).thenReturn(entryGroups);
    }

    private LogEntry createEntry(long instant) {
        LogEntry entry = new LogEntry();
        entry.setDate(new LocalDateTime(instant));
        return entry;
    }

}
