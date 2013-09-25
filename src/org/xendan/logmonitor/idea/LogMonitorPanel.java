package org.xendan.logmonitor.idea;

import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.project.Project;
import org.xendan.logmonitor.idea.model.LogMonitorPanelModel;
import org.xendan.logmonitor.model.Environment;
import org.xendan.logmonitor.model.MatchConfig;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * User: id967161
 * Date: 05/09/13
 */
public class LogMonitorPanel implements CreatePatternListener {
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

    public LogMonitorPanel(LogMonitorPanelModel model, Project project, LogMonitorSettingsConfigurable logMonitorSettingsConfigurable) {
        this.model = model;
        this.logMonitorSettingsConfigurable = logMonitorSettingsConfigurable;
        init(project);
    }

    private void init(Project project) {
        logTree.addMouseListener(new LogDisplayListener());
        treePanel.getViewport().remove(logTree);
        logTree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode("Loading...")));
        linkPanel = new JEditorPane();
        linkPanel.setContentType("text/html");
        linkPanel.setText("No configuration found.<a href='open_config'> Configure...</a>");
        linkPanel.addHyperlinkListener(new OpenConfigurationListener());
        linkPanel.setEditable(false);
        linkPanel.setOpaque(false);
        treePanel.setViewportView(linkPanel);
        console = new ConsoleViewImpl(project, false);
        consolePanel.setLayout(new BoxLayout(consolePanel, BoxLayout.PAGE_AXIS));
        consolePanel.add(console.getComponent());
        createPattern.addActionListener(new CreatePatternActionListener());
        clearButton.addActionListener(new ClearListener());
    }

    public void refresh() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                TreeModel treeModel = model.rebuildTreeModel();
                if (treeModel != null) {
                    treePanel.getViewport().remove(linkPanel);
                    treePanel.setViewportView(logTree);
                    logTree.setModel(treeModel);
                }

            }
        });
    }

    public void onEntriesAdded(Environment environment) {
        refresh();
    }


    private class LogDisplayListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            selectedPath = logTree.getPathForLocation(e.getX(), e.getY());
            if (selectedPath != null) {
                String message = model.getMessage(selectedPath);
                if (message != null) {
                    console.clear();
                    console.print(message, ConsoleViewContentType.ERROR_OUTPUT);
                }
            }
        }
    }

    @Override
    public void onMathConfigAdded(MatchConfig matcher) {
        model.addMatchConfig(matcher, selectedPath);
        refresh();
    }

    private class CreatePatternActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            /*
            CreatePattern dialog = new CreatePattern(model.getContent(selectedPath), LogMonitorPanel.this);
            dialog.pack();
            dialog.setVisible(true);
            */
        }
    }


    private class ClearListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            model.clearAll(selectedPath);
            refresh();
        }
    }

    private class OpenConfigurationListener implements HyperlinkListener {
        @Override
        public void hyperlinkUpdate(HyperlinkEvent e) {
            if (HyperlinkEvent.EventType.ACTIVATED.equals(e.getEventType())) {
                ConfigureDialog dialog = new ConfigureDialog(logMonitorSettingsConfigurable);
                dialog.setLocationRelativeTo(null);
                dialog.pack();
                dialog.setVisible(true);
            }
        }
    }
}
