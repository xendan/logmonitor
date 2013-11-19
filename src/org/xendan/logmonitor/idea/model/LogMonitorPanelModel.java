package org.xendan.logmonitor.idea.model;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.jgoodies.binding.value.ValueHolder;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.xendan.logmonitor.dao.ConfigurationDao;
import org.xendan.logmonitor.idea.BaseDialog;
import org.xendan.logmonitor.idea.MatchConfigForm;
import org.xendan.logmonitor.model.*;
import org.xendan.logmonitor.parser.EntryAddedListener;
import org.xendan.logmonitor.parser.PatternUtils;
import org.xendan.logmonitor.read.Serializer;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: id967161
 * Date: 10/09/13
 */
public class LogMonitorPanelModel {

    private static final DateTimeFormatter HOURS_MINUTES = DateTimeFormat.forPattern("HH:mm");

    public static final String LOADING = "Loading...";
    public static final String GROUP_DISPLAY_ID = "logmonitor messages";
    private final ConfigurationDao dao;
    private final Serializer serializer;
    private final EntryAddedListener listener;
    private Map<Environment, LocalDateTime> updateSince = new HashMap<Environment, LocalDateTime>();
    private Map<Environment, LocalDateTime> nextUpdate = new HashMap<Environment, LocalDateTime>();

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
            node.add(createMatchNode(matchConfig, environment, true));
        }
        return node;
    }

    private DefaultMutableTreeNode createMatchNode(MatchConfig matchConfig, Environment environment, boolean addIsLoading) {
        MatchConfigObject matchConfigObject = new MatchConfigObject(matchConfig);
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(matchConfigObject);
        List<LogEntryGroup> groups = dao.getMatchedEntryGroups(matchConfig, environment);
        List<LogEntry> entries = dao.getNotGroupedMatchedEntries(matchConfig, environment);
        matchConfigObject.setChildNum(groups.size() + entries.size());
        if (!groups.isEmpty() || !entries.isEmpty()) {
            return addGroupsAndEntries(node, groups, entries);
        }
        if (addIsLoading) {
            node.add(new DefaultMutableTreeNode(LOADING));
        }
        return node;

    }

    private DefaultMutableTreeNode addGroupsAndEntries(DefaultMutableTreeNode node, List<LogEntryGroup> groups, List<LogEntry> entries) {
        for (LogEntryGroup group : groups) {
            node.add(createLogEntryGroupNode(group, null, null));
        }
        for (LogEntry entry : entries) {
            node.add(createEntryNode(entry));
        }
        return node;
    }

    private MutableTreeNode createLogEntryGroupNode(LogEntryGroup group, List<LogEntry> newEntries, LocalDateTime since) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(new GroupObject(group));
        for (LogEntry logEntry : group.getEntries()) {
            if (newEntries != null && (since == null || logEntry.getDate().isAfter(since))) {
                newEntries.add(logEntry);
            }
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

    public void onEntriesAdded(LocalDateTime since, Environment environment, DefaultTreeModel model) {
        updateSince.put(environment, since);
        nextUpdate.put(environment, new LocalDateTime(System.currentTimeMillis() + environment.getUpdateInterval() * 60 * 1000));
        List<LogEntry> newEntries = new ArrayList<LogEntry>();
        DefaultMutableTreeNode envNode = findNode((DefaultMutableTreeNode) model.getRoot(), environment);
        for (MatchConfig matchConfig : environment.getMatchConfigs()) {
            DefaultMutableTreeNode node = findNode(envNode, new MatchConfigObject(matchConfig));
            if (node == null) {
                model.insertNodeInto(createMatchNode(matchConfig, environment, false), envNode, envNode.getChildCount());
            } else {
                removeAllChild(model, node);
                MatchConfigObject configObject = (MatchConfigObject) node.getUserObject();
                int itemsNum = 0;
                for (LogEntryGroup group : dao.getMatchedEntryGroups(matchConfig, environment)) {
                    model.insertNodeInto(createLogEntryGroupNode(group, newEntries, since), node, node.getChildCount());
                    itemsNum++;
                }
                for (LogEntry entry : dao.getNotGroupedMatchedEntries(matchConfig, environment)) {
                    if (since == null || entry.getDate().isAfter(since)) {
                        newEntries.add(entry);
                    }
                    model.insertNodeInto(createEntryNode(entry), node, node.getChildCount());
                    itemsNum++;
                }
                configObject.setChildNum(itemsNum);
            }
        }

        Notifications.Bus.notify(getMessage(newEntries, environment));
    }

    private void removeAllChild(DefaultTreeModel model, DefaultMutableTreeNode node) {
        while (node.getChildCount() != 0) {
            model.removeNodeFromParent((MutableTreeNode) node.getChildAt(0));
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
        final DefaultMutableTreeNode groupNode = getNodeFromPath(path, GroupObject.class);
        if (groupNode != null) {
            final LogEntryGroup group = getObjectFromPath(path, GroupObject.class).getEntity();
            menu.add(new JMenuItem(new CreateGroupedMatchConfig(configuration, group, config.getEntity().getLevel())));
            menu.add(new JMenuItem(new RemoveAction(component, new RemoveGroup(treeModel, groupNode, group), "group", "group and all entries in it" )));
        }
        final DefaultMutableTreeNode lastNode = (DefaultMutableTreeNode) path.getLastPathComponent();
        final Object object = lastNode.getUserObject();
        if (object instanceof Environment) {
            final Environment environment = (Environment) object;
            menu.add(new JMenuItem(new RemoveAction(component, new RemoveEntriesInEnvironment(treeModel, lastNode, environment), "log entries in " + environment , "all log entries found in " + environment)));
        } else if (object instanceof MatchConfigObject) {
                final MatchConfig matchConfig = ((MatchConfigObject) object).getEntity();
                menu.add(new JMenuItem(new RemoveAction(component, new RemoveEntriesMatching(lastNode, treeModel), "log entries matching " + matchConfig , "all log entries matching " + matchConfig)));
        } else if (object instanceof EntryObject) {
            final DefaultMutableTreeNode parent = (DefaultMutableTreeNode) lastNode.getParent();
            final LogEntry entry = ((EntryObject) object).getEntity();
            menu.add(new JMenuItem(new RemoveAction(component, new RemoveSingleEntry(parent, lastNode, treeModel, entry), "log entry", "log entry" )));
        }
        return menu;
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
        if (userObject instanceof Environment) {
            return updateSince.get(userObject);
        }
        return findSince((DefaultMutableTreeNode) node.getParent());
    }

    public String getTooltipText(DefaultMutableTreeNode node) {
        Object userObject = node.getUserObject();
        if (userObject instanceof Environment) {
            LocalDateTime nextUpdate = this.nextUpdate.get(userObject);
            if (nextUpdate !=  null) {
                return "Next update at: " + HOURS_MINUTES.print(nextUpdate) + "\n new: " + findNewCount(node, 0);
            }
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

    private interface ConsoleDisplayable {
        String toConsoleString();
    }

    private static class EntityObject<E extends BaseObject> {
        protected final E entity;

        private EntityObject(E entity) {
            this.entity = entity;
        }

        public E getEntity() {
            return entity;
        }
    }

    public class MatchConfigObject extends EntityObject<MatchConfig> implements ConsoleDisplayable {
        private int childNum;

        public MatchConfigObject(MatchConfig matchConfig) {
            super(matchConfig);
        }

        public void setChildNum(int childNum) {
            this.childNum = childNum;
        }

        @Override
        public String toString() {
            return entity.toString() + "(" + childNum + ")";
        }

        @Override
        public String toConsoleString() {
            return entity.toString() + "\nLEVEL>=" + entity.getLevel() + "\nPattern:\n" + entity.getMessage();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            MatchConfigObject that = (MatchConfigObject) o;

            if (entity != null ? !entity.equals(that.entity) : that.entity != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return entity != null ? entity.hashCode() : 0;
        }
    }

    public static class EntryObject extends EntityObject<LogEntry> implements ConsoleDisplayable {

        public EntryObject(LogEntry entry) {
            super(entry);
        }

        @Override
        public String toString() {
            return entity.getLevel() + ":" + entity.getDate() + StringUtils.abbreviate(entity.getMessage(), 10);
        }

        @Override
        public String toConsoleString() {
            return entity.getLevel() + ":" + entity.getDate() + "\n" + getMessage();
        }

        protected String getMessage() {
            return entity.getMessage();
        }

        public boolean isError() {
            return Level.toLevel(entity.getLevel()).isGreaterOrEqual(Level.ERROR);
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

    private static class GroupObject extends EntityObject<LogEntryGroup> implements ConsoleDisplayable {

        public GroupObject(LogEntryGroup group) {
            super(group);
        }

        @Override
        public String toString() {
            return entity.getEntries().size() + " similar entries";
        }

        @Override
        public String toConsoleString() {
            return toString() + " matched by \n" + entity.getMessagePattern();
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
            for (Environment environment : copy) {
                environment.getMatchConfigs().add(config);
            }
            matchConfigForm.setEnvironments(new ValueHolder(copy));
            matchConfigForm.setIsSpecific();
            BaseDialog dialog = new BaseDialog(new OnMatchConfigOkAction(config, copy), matchConfigForm.getContentPanel());
            dialog.setTitleAndShow("Add new match config");
        }

        private void doSaveSettings(MatchConfig config, List<Environment> copies, List<Environment> originals) {
            for (int i = 0; i < copies.size(); i++) {
                if (copies.get(i).getMatchConfigs().contains(config)) {
                    dao.addMatchConfig(originals.get(i), config);
                    listener.onEntriesAdded(originals.get(i), null);
                }
            }
        }

        private class OnMatchConfigOkAction extends Thread implements OnOkAction {
            private final MatchConfig config;
            private final List<Environment> copy;

            private OnMatchConfigOkAction(MatchConfig config, List<Environment> copy) {
                this.config = config;
                this.copy = copy;
            }

            @Override
            public boolean doAction() {
                start();
                return true;
            }

            @Override
            public void run() {
                doSaveSettings(config, copy, configuration.getEnvironments());
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
                dao.remove(((EntityObject)child.getUserObject()).getEntity());
            }
        }


    }
}
