package org.xendan.logmonitor.idea;

import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.project.Project;

import javax.swing.*;
import javax.swing.tree.TreePath;
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
    private LogMonitorPanelModel model;
    private Project project;

    public LogMonitorPanel(LogMonitorPanelModel model) {
        this.model = model;
        logTree.addMouseListener(new LogDisplayListener());
    }

    public void refresh() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                logTree.setModel(model.getTreeModel());
            }
        });
    }

    public void setProject(Project project) {
        this.project = project;
        console = new ConsoleViewImpl(project, false);
        consolePanel.setLayout(new BoxLayout(consolePanel, BoxLayout.PAGE_AXIS));
        consolePanel.add(console.getComponent());
    }


    private class LogDisplayListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            TreePath path = logTree.getPathForLocation(e.getX(), e.getY());
            if (path != null) {
                String message = model.getMessage(path);
                if (message != null) {
                    console.clear();
                    console.print(message, ConsoleViewContentType.ERROR_OUTPUT);
                }
            }
        }
    }
}
