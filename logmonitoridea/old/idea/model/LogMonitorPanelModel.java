package org.xendan.logmonitor.idea.model;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import org.apache.log4j.Level;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.xendan.logmonitor.HomeResolver;
import org.xendan.logmonitor.dao.Callback;
import org.xendan.logmonitor.dao.DefaultCallBack;
import org.xendan.logmonitor.dao.LogService;
import org.xendan.logmonitor.idea.model.node.*;
import org.xendan.logmonitor.idea.model.task.ShowLogAroundAction;
import org.xendan.logmonitor.model.*;
import org.xendan.logmonitor.parser.DateParser;
import org.xendan.logmonitor.parser.EntryStatusListener;
import org.xendan.logmonitor.parser.LogParser;
import org.xendan.logmonitor.read.Serializer;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.List;

/**
 * User: id967161
 * Date: 10/09/13
 */
public class LogMonitorPanelModel {
    public static final String LOADING = "Loading...";
    public static final String GROUP_DISPLAY_ID = "logmonitor messages";

    public static final DateTimeFormatter SHORT_DATE = DateTimeFormat.forPattern("dd HH:mm:ss");
    public static final int MSG_WIDTH = 30;

    private final LogService dao;
    private final Serializer serializer;
    private final EntryStatusListener listener;
    private final HomeResolver homeResolver;
    private Map<Environment, Boolean> newEntriesCalculated = new HashMap<Environment, Boolean>();
    private List<Configuration> configs;
    private boolean configsLoading;
    private Callback<Boolean> hasConfigsCallback;
    private BuildTreeCallback buildTreeCallback;
    private Project project;

    private Map<Environment, Message> environmentMessages = new HashMap<Environment, Message>();

    public LogMonitorPanelModel(Project project, LogService dao, Serializer serializer, EntryStatusListener listener, HomeResolver homeResolver) {
        this.project = project;
        this.dao = dao;
        this.serializer = serializer;
        this.listener = listener;
        this.homeResolver = homeResolver;
    }

    public void hasConfig(final Callback<Boolean> callback) {
        hasConfigsCallback = callback;
        doGetConfigs();
    }

    private synchronized void doGetConfigs() {
        if (!configsLoading) {
            configsLoading = true;
            dao.getConfigs(new DefaultCallBack<List<Configuration>>() {
                @Override
                public void onAnswer(List<Configuration> answer) {
                    configs = answer;
                    if (hasConfigsCallback != null) {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                hasConfigsCallback.onAnswer(!configs.isEmpty());
                            }
                        });
                    }
                    if (buildTreeCallback != null) {
                        buildTreeCallback.onAnswer(configs);
                    }
                    configsLoading = false;
                }
            });
        }
    }

    public void initTreeModel(final Callback<DefaultTreeModel> callback) {
        buildTreeCallback = new BuildTreeCallback(callback);
        doGetConfigs();
    }

    private MutableTreeNode createConfigNode(Configuration configuration, DefaultTreeModel treeModel) {
        DefaultMutableTreeNode configNode = new DefaultMutableTreeNode(configuration);
        for (Environment environment : configuration.getEnvironments()) {
            configNode.add(createEnvironment(environment, treeModel));
        }
        return configNode;
    }

    private MutableTreeNode createEnvironment(Environment environment, final DefaultTreeModel treeModel) {
        final DefaultMutableTreeNode node = new DefaultMutableTreeNode(new EnvironmentObject(environment));
        for (MatchConfig matchConfig : environment.getMatchConfigs()) {
            insertNode(createMatchNode(matchConfig, environment, true, treeModel), node, treeModel);
        }
        return node;
    }

    private DefaultMutableTreeNode createMatchNode(MatchConfig matchConfig, Environment environment, boolean addIsLoading, final DefaultTreeModel treeModel) {
        MatchConfigObject matchConfigObject = new MatchConfigObject(matchConfig);
        final DefaultMutableTreeNode node = new DefaultMutableTreeNode(matchConfigObject);
        final DefaultMutableTreeNode loading = new DefaultMutableTreeNode(LOADING);
        if (addIsLoading) {
            insertNode(loading, node, treeModel);
        }

        dao.getMatchedEntryGroups(matchConfig, environment, new DefaultCallBack<List<LogEntryGroup>>() {
            @Override
            public void onAnswer(List<LogEntryGroup> groups) {
                for (LogEntryGroup group : groups) {
                    removeLoading(loading, node, treeModel);
                    insertNode(createLogEntryGroupNode(group, null, null), node, treeModel);

                }
            }
        });
        dao.getNotGroupedMatchedEntries(matchConfig, environment, new DefaultCallBack<List<LogEntry>>() {
            @Override
            public void onAnswer(List<LogEntry> entries) {
                for (LogEntry entry : entries) {
                    removeLoading(loading, node, treeModel);
                    insertNode(createEntryNode(entry), node, treeModel);
                }

            }
        });
        return node;
    }

    private DefaultMutableTreeNode createLogEntryGroupNode(LogEntryGroup group, List<LogEntry> newEntries, LocalDateTime since) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(new GroupObject(group));
        for (LogEntry logEntry : sorted(group.getEntries(), newEntries, since)) {
            node.add(new DefaultMutableTreeNode(new GroupedEntryObject(logEntry, group)));
        }
        return node;
    }

    private List<LogEntry> sorted(List<LogEntry> entries, final List<LogEntry> newEntries, LocalDateTime since) {
        for (LogEntry entry : entries) {
            if (newEntries != null && (since == null || entry.getDate().isAfter(since))) {
                newEntries.add(entry);
            }
        }
        Collections.sort(entries, new EntriesComparator(newEntries));
        return entries;
    }

    private DefaultMutableTreeNode createEntryNode(LogEntry entry) {
        return new DefaultMutableTreeNode(new EntryObject(entry));
    }

    public String getMessage(TreePath path) {
        if (path.getLastPathComponent() instanceof MutableTreeNode) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
            if (node.getUserObject() instanceof ConsoleDisplayable) {
                return ((ConsoleDisplayable) node.getUserObject()).toConsoleString();
            }
        }
        return path.getLastPathComponent().toString();
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

    private DefaultMutableTreeNode getNodeFromPath(TreePath path, Class<?> objectClass) {
        if (path == null) {
            return null;
        }
        for (int i = 0; i < path.getPathCount(); i++) {
            Object component = path.getPathComponent(i);
            if (component instanceof DefaultMutableTreeNode) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) component;
                Object obj = node.getUserObject();
                if (obj != null && objectClass.equals(obj.getClass())) {
                    return node;
                }
            }
        }
        return null;
    }

    public void onEntriesNotFound(final Environment environment, final DefaultTreeModel model) {
        final DefaultMutableTreeNode envNode = getEnvironmentNode(environment, model);
        removeAllChild(model, envNode);
        model.insertNodeInto(new DefaultMutableTreeNode(getFileNotMatchMessage(environment, envNode)), envNode, 0);
    }

    public DefaultMutableTreeNode getEnvironmentNode(Environment environment, DefaultTreeModel model) {
        return findNode((DefaultMutableTreeNode) model.getRoot(), new EnvironmentObject(environment));
    }


    public String getFileNotMatchMessage(Environment environment, DefaultTreeModel treeModel) {
        return getFileNotMatchMessage(environment, getEnvironmentNode(environment, treeModel));
    }

    private String getFileNotMatchMessage(Environment environment, DefaultMutableTreeNode envNode) {
        Configuration config = (Configuration) ((DefaultMutableTreeNode) envNode.getParent()).getUserObject();
        return "File " + environment.getPath() + " do not match pattern " + config.getLogPattern();
    }


    public void onEntriesAdded(final Environment environment, final DefaultTreeModel model) {
        newEntriesCalculated.put(environment, false);
        final List<LogEntry> newEntries = new ArrayList<LogEntry>();
        final DefaultMutableTreeNode envNode = getEnvironmentNode(environment, model);
        for (final MatchConfig matchConfig : environment.getMatchConfigs()) {
            final DefaultMutableTreeNode node = findNode(envNode, new MatchConfigObject(matchConfig));
            if (node == null) {
                swingInvokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        model.insertNodeInto(createMatchNode(matchConfig, environment, false, model), envNode, 0);
                    }
                });
            } else {
                removeAllChild(model, node);
                dao.getMatchedEntryGroups(matchConfig, environment, new DefaultCallBack<List<LogEntryGroup>>() {
                    @Override
                    public void onAnswer(List<LogEntryGroup> groups) {
                        for (LogEntryGroup group : groups) {
                            insertNode(createLogEntryGroupNode(group, newEntries, environment.getLastUpdate()), node, model);
                        }
                        checkShowNotifications(newEntries, environment);
                    }
                });
                dao.getNotGroupedMatchedEntries(matchConfig, environment, new DefaultCallBack<List<LogEntry>>() {
                    @Override
                    public void onAnswer(List<LogEntry> entries) {
                        for (LogEntry entry : sorted(entries, newEntries, environment.getLastUpdate())) {
                            insertNode(createEntryNode(entry), node, model);
                        }
                        checkShowNotifications(newEntries, environment);
                    }
                });
            }
        }
    }

    private void checkShowNotifications(List<LogEntry> newEntries, Environment environment) {
        if (Boolean.TRUE.equals(newEntriesCalculated.get(environment))) {
            Notifications.Bus.notify(getMessage(newEntries, environment));
        }
        newEntriesCalculated.put(environment, Boolean.TRUE);
    }

    private void removeLoading(final DefaultMutableTreeNode loading, DefaultMutableTreeNode parent, final DefaultTreeModel treeModel) {
        if (loading.getParent() != null) {
            if (parent.getParent() == null) {
                parent.remove(loading);
            } else {
                swingInvokeAndWait(
                        new Runnable() {
                            @Override
                            public void run() {
                                treeModel.removeNodeFromParent(loading);
                            }
                        }
                );

            }
        }
    }

    private void insertNode(final DefaultMutableTreeNode newChild, final DefaultMutableTreeNode root, final DefaultTreeModel model) {
        //not yet in model
        if (root.getParent() == null) {
            root.add(newChild);
        } else {
            swingInvokeAndWait(new Runnable() {
                @Override
                public void run() {
                    model.insertNodeInto(newChild, root, root.getChildCount());
                }
            });
        }
    }

    private void swingInvokeAndWait(Runnable runnable) {
        if (SwingUtilities.isEventDispatchThread()) {
            runnable.run();
        } else {
            try {
                SwingUtilities.invokeAndWait(runnable);
            } catch (Exception e) {
                throw new IllegalStateException("Error invoking task", e);
            }
        }
    }

    private void removeAllChild(final DefaultTreeModel model, final DefaultMutableTreeNode node) {
        while (node.getChildCount() != 0) {
            swingInvokeAndWait(new Runnable() {
                @Override
                public void run() {
                    model.removeNodeFromParent((MutableTreeNode) node.getChildAt(0));
                }
            });
        }
    }

    private Notification getMessage(List<LogEntry> newEntries, Environment environment) {
        if (!newEntries.isEmpty()) {
            return new Notification(GROUP_DISPLAY_ID,
                    "Found Message" + (newEntries.size() == 1 ? "" : "s") + newEntries.size(),
                    "Something about new messages",
                    NotificationType.WARNING);
        }
        return new Notification(GROUP_DISPLAY_ID, environment + " log updated", "No new log entries found ", NotificationType.INFORMATION);
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

    public JPopupMenu getContextMenu(TreePath path, Runnable openConfigDialog, final DefaultTreeModel treeModel, Component component) {
        JPopupMenu menu = new JPopupMenu();
        menu.add(new JMenuItem(new OpenConfig(openConfigDialog)));
        MatchConfigObject config = getObjectFromPath(path, MatchConfigObject.class);
        Configuration configuration = getObjectFromPath(path, Configuration.class);
        menu.add(new JMenuItem(newCreateMatchAction(configuration, "Create new match...", Level.ERROR.toString(), "")));
        final DefaultMutableTreeNode groupNode = getNodeFromPath(path, GroupObject.class);
        if (groupNode != null) {
            final LogEntryGroup group = getObjectFromPath(path, GroupObject.class).getEntity();
            menu.add(new JMenuItem(newCreateMatchAction(configuration, "Create match for group...", config.getEntity().getLevel(), group.getMessagePattern())));
            menu.add(new JMenuItem(new RemoveAction(component, new RemoveGroup(treeModel, groupNode, group), "group", "group and all entries in it")));
        }
        final DefaultMutableTreeNode lastNode = (DefaultMutableTreeNode) path.getLastPathComponent();
        final Object object = lastNode.getUserObject();
        if (object instanceof EnvironmentObject) {
            final Environment environment = ((EnvironmentObject) object).getEntity();
            menu.add(new JMenuItem(new RemoveAction(component, new RemoveEntriesInEnvironment(treeModel, lastNode, environment), "log entries in " + environment, "all log entries found in " + environment)));
        } else if (object instanceof MatchConfigObject) {
            final MatchConfig matchConfig = ((MatchConfigObject) object).getEntity();
            menu.add(new JMenuItem(new RemoveAction(component, new RemoveEntriesMatching(lastNode, treeModel), "log entries matching " + matchConfig, "all log entries matching " + matchConfig)));
            Environment environment = getObjectFromPath(path, EnvironmentObject.class).getEntity();
            menu.add(new JMenuItem(new RemoveAction(component, new RemoveMatchAndEntries(lastNode, treeModel, environment), "match and entries" + matchConfig, "match and entries" + matchConfig)));
        } else if (object instanceof EntryObject) {
            Environment environment = getObjectFromPath(path, EnvironmentObject.class).getEntity();
            final DefaultMutableTreeNode parent = (DefaultMutableTreeNode) lastNode.getParent();
            final LogEntry entry = ((EntryObject) object).getEntity();
            String date = new DateParser().getDateAsString(configuration.getLogPattern(), entry.getDate());
            String logPattern = new LogParser(configuration.getLogPattern(), environment).getRegExpStr();
            menu.add(new JMenuItem(new ShowLogAroundAction(environment, homeResolver, date, logPattern, project)));
            menu.add(new JMenuItem(new RemoveAction(component, new RemoveSingleEntry(parent, lastNode, treeModel, entry), "log entry", "log entry")));
        }
        return menu;
    }

    private AbstractAction newCreateMatchAction(Configuration configuration, String name, String level, String message) {
        return new CreateMatchAction(serializer, listener, dao, configuration, name, level, message);
    }

    public boolean isNodeUpdated(DefaultMutableTreeNode node) {
        return isNodeUpdated(node, null);
    }

    public boolean isNodeUpdated(DefaultMutableTreeNode node, LocalDateTime since) {
        Object userObject = node.getUserObject();
        if (userObject instanceof String) {
            //userObject is String - it is initial tree.
            return false;
        }
        if (since == null) {
            if (userObject != null && !(userObject instanceof Configuration)) {
                since = findSince(node);
            }
        }
        if (userObject instanceof EntryObject) {
            LogEntry entry = ((EntryObject) userObject).getEntity();
            return since != null && entry.getDate().isAfter(since);
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getChildAt(i);
            if (isNodeUpdated(child, since)) {
                return true;
            }

        }
        return false;
    }

    private LocalDateTime findSince(DefaultMutableTreeNode node) {
        Object userObject = node.getUserObject();
        if (userObject instanceof EnvironmentObject) {
            return ((EnvironmentObject) userObject).getEntity().getLastUpdate();
        }
        return findSince((DefaultMutableTreeNode) node.getParent());
    }

    public String getTooltipText(DefaultMutableTreeNode node) {
        Object userObject = node.getUserObject();
        if (userObject instanceof EnvironmentObject) {
            String nextUpdate = (((EnvironmentObject) userObject).getEntity()).getNextUpdate();
            String result = "Next update " + nextUpdate;
            if (!Environment.SOON.equals(nextUpdate)) {
                result += "\n new: " + findNewCount(node, 0);
            }
            return result;
        }
        return "";
    }

    private int findNewCount(DefaultMutableTreeNode node, int count) {
        if (isNodeUpdated(node)) {
            count++;
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            count += findNewCount((DefaultMutableTreeNode) node.getChildAt(i), count);
        }
        return count;
    }

    public String getEnvironmentStatusMessages(Environment environment, String message, boolean isError) {
        environmentMessages.put(environment, new Message(message, isError));
        String html = "<html><body>";
        for (Map.Entry<Environment, Message> entry : environmentMessages.entrySet()) {
            html += getEnvironmentMessage(entry.getKey(), entry.getValue().getMessage(), entry.getValue().isError()) + "<br />";
        }
        return html + "</body></html>";
    }

    private String getEnvironmentMessage(Environment environment, String message, boolean error) {
        return "<span class='environment'>" + environment.getName() + "</span>: <span class='" + (error ? "error" : "info") + "'>" + message + "</span>";
    }

    private static class Message {
        private boolean error;
        private String message;

        private Message(String message, boolean error) {
            this.error = error;
            this.message = message;
        }

        public boolean isError() {
            return error;
        }

        public String getMessage() {
            return message;
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

    private class RemoveAction extends AbstractAction {


        private final Component component;
        private final Runnable onYes;
        private final String fullDescription;

        public RemoveAction(Component component, Runnable onYes, String what, String fullDescription) {
            super("Remove " + what);
            this.component = component;
            this.onYes = onYes;
            this.fullDescription = fullDescription;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JBPopupFactory.getInstance()
                    .createConfirmation("Delete " + fullDescription, "Yes", "Cancel", onYes, 0)
                    .showInCenterOf(component);

        }
    }

    private class RemoveSingleEntry implements Runnable {
        private final DefaultMutableTreeNode parent;
        private final DefaultMutableTreeNode lastNode;
        private final DefaultTreeModel treeModel;
        private final LogEntry entry;

        public RemoveSingleEntry(DefaultMutableTreeNode parent, DefaultMutableTreeNode lastNode, DefaultTreeModel treeModel, LogEntry entry) {
            this.parent = parent;
            this.lastNode = lastNode;
            this.treeModel = treeModel;
            this.entry = entry;
        }

        @Override
        public void run() {
            Object parentObject = parent.getUserObject();
            boolean simpleRemove = true;
            if (parentObject instanceof GroupObject) {
                if (parent.getChildCount() == 2) {
                    DefaultMutableTreeNode other = (parent.getFirstLeaf() == lastNode) ? parent.getLastLeaf() : parent.getFirstLeaf();
                    MutableTreeNode grandParent = (MutableTreeNode) parent.getParent();
                    int index = treeModel.getIndexOfChild(grandParent, parent);
                    treeModel.removeNodeFromParent(parent);
                    //TODO create normal entry message
                    treeModel.insertNodeInto(other, grandParent, index);
                    LogEntryGroup group = ((GroupObject) parentObject).getEntity();
                    group.getEntries().remove(entry);
                    dao.remove(group);
                    simpleRemove = false;
                }
            }
            if (simpleRemove) {
                treeModel.removeNodeFromParent(lastNode);
            }
            dao.remove(entry);

        }
    }

    private class RemoveEntriesInEnvironment implements Runnable {
        private final DefaultTreeModel treeModel;
        private final DefaultMutableTreeNode lastNode;
        private final Environment environment;

        public RemoveEntriesInEnvironment(DefaultTreeModel treeModel, DefaultMutableTreeNode lastNode, Environment environment) {
            this.treeModel = treeModel;
            this.lastNode = lastNode;
            this.environment = environment;
        }

        @Override
        public void run() {
            removeAllChild(treeModel, lastNode);
            dao.removeAllEntries(environment);
        }
    }

    private class RemoveGroup implements Runnable {
        private final DefaultTreeModel treeModel;
        private final DefaultMutableTreeNode groupNode;
        private final LogEntryGroup group;

        public RemoveGroup(DefaultTreeModel treeModel, DefaultMutableTreeNode groupNode, LogEntryGroup group) {
            this.treeModel = treeModel;
            this.groupNode = groupNode;
            this.group = group;
        }

        @Override
        public void run() {
            treeModel.removeNodeFromParent(groupNode);
            dao.remove(group);
        }
    }


    private class RemoveEntriesMatching implements Runnable {
        private final DefaultMutableTreeNode lastNode;
        private final DefaultTreeModel treeModel;

        public RemoveEntriesMatching(DefaultMutableTreeNode lastNode, DefaultTreeModel treeModel) {
            this.lastNode = lastNode;
            this.treeModel = treeModel;
        }

        @Override
        public void run() {
            while (lastNode.getChildCount() != 0) {
                DefaultMutableTreeNode child = (DefaultMutableTreeNode) lastNode.getChildAt(0);
                treeModel.removeNodeFromParent(child);
                dao.remove(((EntityObject) child.getUserObject()).getEntity());
            }
        }


    }

    private class BuildTreeCallback extends DefaultCallBack<List<Configuration>> {
        private final Callback<DefaultTreeModel> callback;

        public BuildTreeCallback(Callback<DefaultTreeModel> callback) {
            this.callback = callback;
        }

        @Override
        public void onAnswer(List<Configuration> configs) {
            if (configs.isEmpty()) {
                callback.onAnswer(null);
                return;
            }
            DefaultMutableTreeNode root = new DefaultMutableTreeNode();
            DefaultTreeModel treeModel = new DefaultTreeModel(root);
            for (Configuration configuration : configs) {
                root.add(createConfigNode(configuration, treeModel));
            }
            callback.onAnswer(treeModel);
        }
    }


    private class RemoveMatchAndEntries implements Runnable {
        private final DefaultMutableTreeNode lastNode;
        private final DefaultTreeModel treeModel;
        private final Environment environment;

        public RemoveMatchAndEntries(DefaultMutableTreeNode node, DefaultTreeModel treeModel, Environment environment) {
            this.lastNode = node;
            this.treeModel = treeModel;
            this.environment = environment;
        }

        @Override
        public void run() {
            treeModel.removeNodeFromParent(lastNode);
            dao.removeMatchConfig(((MatchConfigObject) lastNode.getUserObject()).getEntity(), environment);
        }
    }

}
