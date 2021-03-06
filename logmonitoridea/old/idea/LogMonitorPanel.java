package org.xendan.logmonitor.idea;

import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import org.apache.log4j.Logger;
import org.xendan.logmonitor.dao.Callback;
import org.xendan.logmonitor.dao.DefaultCallBack;
import org.xendan.logmonitor.idea.model.LogMonitorPanelModel;
import org.xendan.logmonitor.idea.model.node.EntryObject;
import org.xendan.logmonitor.idea.model.node.EnvironmentObject;
import org.xendan.logmonitor.idea.model.node.MatchConfigObject;
import org.xendan.logmonitor.model.Configuration;
import org.xendan.logmonitor.model.Environment;
import org.xendan.logmonitor.model.MatchConfig;

import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
    private JButton clearButton;
    private JScrollPane treePanel;
    private JEditorPane statusEditorPane;
    private LogMonitorPanelModel model;
    private final LogMonitorSettingsConfigurable logMonitorSettingsConfigurable;
    private TreePath selectedPath;
    private JEditorPane linkPanel;
    private String errorLog = "";
    private final Runnable openConfigDialog;
    private boolean treeModelInited;
//    private static final Logger logger = LoggerFactory.getInstance().getLoggerInstance(LogMonitorPanel.class.getCanonicalName());
    private static final Logger logger = Logger.getLogger(LogMonitorPanel.class);

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

        model.hasConfig(new DefaultCallBack<Boolean>() {
            @Override
            public void onAnswer(Boolean answer) {
                if (answer) {
                    linkPanel.setText(LogMonitorPanelModel.LOADING);
                } else {
                    linkPanel.setText("No configuration found.<a href='open_config'> Configure...</a>");
                }
            }
        });

        linkPanel.addHyperlinkListener(new OpenConfigurationListener());
        linkPanel.setEditable(false);
        linkPanel.setOpaque(false);
        treePanel.setViewportView(linkPanel);
        console = new ConsoleViewImpl(project, false);
        consolePanel.setLayout(new BoxLayout(consolePanel, BoxLayout.PAGE_AXIS));
        consolePanel.add(console.getComponent());
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logMonitorSettingsConfigurable.tmpReload();
            }
        });
        statusEditorPane.setContentType("text/html");
        ((HTMLDocument)statusEditorPane.getDocument()).getStyleSheet().addRule(".environment {font-weight:bold;}");
        ((HTMLDocument)statusEditorPane.getDocument()).getStyleSheet().addRule(".error {color:red;}");
        ((HTMLDocument)statusEditorPane.getDocument()).getStyleSheet().addRule(".info {color:green;}");

    }

    public void onException(final Throwable e) {
        errorLog += "\n" + e.getMessage();
        linkPanel.setText(errorLog);
        logger.error(e);
    }

    public void onEntriesAdded(final Environment environment) {
        initModel(new DefaultCallBack<Void>() {
            @Override
            public void onAnswer(Void answer) {
                model.onEntriesAdded(environment, (DefaultTreeModel) logTree.getModel());
            }
        });

    }

    public synchronized void initModel(final Callback<Void> onLoaded) {
        if (!treeModelInited) {
            model.initTreeModel(new Callback<DefaultTreeModel>() {
                @Override
                public void onAnswer(final DefaultTreeModel treeModel) {
                    if (treeModel != null) {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                treePanel.getViewport().remove(linkPanel);
                                treePanel.setViewportView(logTree);
                                logTree.setModel(treeModel);
                                treeModelInited = true;
                                onLoaded.onAnswer(null);
                            }
                        });
                    }
                }

                @Override
                public void onFail(Throwable error) {
                    onException(error);
                }
            });
        } else {
            onLoaded.onAnswer(null);
        }
    }

    public void updateDownloadAndParseStatus(Environment environment, String message, boolean isError) {
        statusEditorPane.setText(model.getEnvironmentStatusMessages(environment, message, isError));
    }

    public void onEntriesNotFound(Environment environment) {
        DefaultTreeModel treeModel = (DefaultTreeModel) logTree.getModel();
        model.onEntriesNotFound(environment, treeModel);
        updateDownloadAndParseStatus(environment, model.getFileNotMatchMessage(environment, treeModel), true);
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
                model.getContextMenu(selectedPath,
                        openConfigDialog,
                        (DefaultTreeModel) logTree.getModel(),
                        LogMonitorPanel.this.contentPanel)
                        .show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }

    public static ImageIcon getImgIcon(String name) {
        return new ImageIcon(LogMonitorPanel.class.getResource("/resources/img/" + name + ".png"));
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
                    setIcon(getImgIcon(name));
                }
                if (model.isNodeUpdated(node)) {
                    setFont(bold);
                } else {
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
            if (userObject instanceof EnvironmentObject) {
                return "environment";
            }
            if (userObject instanceof MatchConfigObject) {
                MatchConfig config = ((MatchConfigObject) userObject).getEntity();
                return config.isGeneral() ? "list-unknown" : "list-error";
            }
            if (userObject instanceof EntryObject) {
                EntryObject entryObject = (EntryObject) userObject;
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
            public boolean canClose() {
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
