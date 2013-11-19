package org.xendan.logmonitor.idea;

import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import org.joda.time.LocalDateTime;
import org.xendan.logmonitor.idea.model.LogMonitorPanelModel;
import org.xendan.logmonitor.idea.model.OnOkAction;
import org.xendan.logmonitor.model.Configuration;
import org.xendan.logmonitor.model.Environment;
import org.xendan.logmonitor.model.MatchConfig;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * User: id967161
 * Date: 05/09/13
 */
public class LogMonitorPanel {
    private ConsoleViewImpl console;
    public JPanel contentPanel;
    private JTree logTree;
    private JPanel consolePanel;
    private JButton createPattern;
    private JButton clearButton;
    private JScrollPane treePanel;
    private LogMonitorPanelModel model;
    private final LogMonitorSettingsConfigurable logMonitorSettingsConfigurable;
    private TreePath selectedPath;
    private JEditorPane linkPanel;
    private String errorLog = "";
    private final Runnable openConfigDialog;
    private boolean treeModelInited;

    public LogMonitorPanel(LogMonitorPanelModel model, Project project, LogMonitorSettingsConfigurable logMonitorSettingsConfigurable) {
        this.model = model;
        this.logMonitorSettingsConfigurable = logMonitorSettingsConfigurable;
        openConfigDialog = new OpenConfigDialog();
        init(project);
    }

    private void init(Project project) {
        logTree.setCellRenderer(new LogTreeCellRenderer());
        logTree.addMouseListener(new LogDisplayListener());
        treePanel.getViewport().remove(logTree);
        logTree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode("Loading...")));
        linkPanel = new JEditorPane();
        linkPanel.setContentType("text/html");
        linkPanel.setText(getInitialText());
        linkPanel.addHyperlinkListener(new OpenConfigurationListener());
        linkPanel.setEditable(false);
        linkPanel.setOpaque(false);
        treePanel.setViewportView(linkPanel);
        console = new ConsoleViewImpl(project, false);
        consolePanel.setLayout(new BoxLayout(consolePanel, BoxLayout.PAGE_AXIS));
        consolePanel.add(console.getComponent());
    }

    public void onException(Exception e) {
        //TODO if tree is shown
        errorLog += "\n" + e.getMessage();
        linkPanel.setText(errorLog);
    }

    private String getInitialText() {
        if (model.hasConfig()) {
            return LogMonitorPanelModel.LOADING;
        }
        return "No configuration found.<a href='open_config'> Configure...</a>";
    }

    public void onEntriesAdded(Environment environment, LocalDateTime since) {
        initModel();
        model.onEntriesAdded(since, environment, (DefaultTreeModel) logTree.getModel());
    }

    public void initModel() {
        if (!treeModelInited) {
            DefaultTreeModel treeModel = model.initTreeModel();
            if (treeModel != null) {
                treePanel.getViewport().remove(linkPanel);
                treePanel.setViewportView(logTree);
                logTree.setModel(treeModel);
                treeModelInited = true;
            }
        }
    }


    private class LogDisplayListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            selectedPath = logTree.getPathForLocation(e.getX(), e.getY());
            if (SwingUtilities.isLeftMouseButton(e)) {
                if (selectedPath != null) {
                    String message = model.getMessage(selectedPath);
                    if (message != null) {
                        console.clear();
                        console.print(message, ConsoleViewContentType.ERROR_OUTPUT);
                    }
                }
            } else {
                model.getContextMenu(selectedPath, openConfigDialog).show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }


    private class OpenConfigurationListener implements HyperlinkListener {
        @Override
        public void hyperlinkUpdate(HyperlinkEvent e) {
            if (HyperlinkEvent.EventType.ACTIVATED.equals(e.getEventType())) {
                openConfigDialog.run();
            }
        }
    }

    private class LogTreeCellRenderer extends DefaultTreeCellRenderer {
        private final Font bold;

        private LogTreeCellRenderer() {
            bold = new Font(logTree.getFont().getName(), Font.BOLD, logTree.getFont().getSize());
        }

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
            if (value instanceof DefaultMutableTreeNode) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
                String name = getIconImgName(node.getUserObject());
                if (name != null) {
                    setIcon(new ImageIcon(getClass().getResource("/org/xendan/logmonitor/idea/img/" + name + ".png")));
                }
                if (model.isNodeUpdated(node)) {
                    setFont(bold);
                } else  {
                    setFont(logTree.getFont());
                }
                setToolTipText(model.getTooltipText(node));
            }
            return this;
        }

        private String getIconImgName(Object userObject) {
            if (userObject instanceof Configuration) {
                return "project";
            }
            if (userObject instanceof Environment) {
                return "environment";
            }
            if (userObject instanceof LogMonitorPanelModel.MatchConfigObject) {
                MatchConfig config = ((LogMonitorPanelModel.MatchConfigObject) userObject).getMatchConfig();
                return config.isGeneral() ? "list-unknown" : "list-error";
            }
            if (userObject instanceof LogMonitorPanelModel.EntryObject) {
                LogMonitorPanelModel.EntryObject entryObject = (LogMonitorPanelModel.EntryObject) userObject;
                return entryObject.isError() ? "error" : "warning";
            }
            return null;
        }
    }

    private class OpenConfigDialog implements Runnable {

        @Override
        public void run() {
            BaseDialog dialog = new BaseDialog(new ConfigureDialogOnOkAction(), logMonitorSettingsConfigurable.getContentPanel());
            logMonitorSettingsConfigurable.reset();
            dialog.setTitleAndShow("Configuration");
        }

        private class ConfigureDialogOnOkAction implements OnOkAction {
            @Override
            public boolean doAction() {
                try {
                    logMonitorSettingsConfigurable.apply();
                    return true;
                } catch (ConfigurationException e) {
                    //TODO show message
                    e.printStackTrace();
                }
                return false;
            }
        }
    }
}
