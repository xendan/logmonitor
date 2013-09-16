package org.xendan.logmonitor.idea;

import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.project.Project;
import org.xendan.logmonitor.model.MatchConfig;

import javax.swing.*;
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
    private LogMonitorPanelModel model;
    private Project project;
    private TreePath selectedPath;

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
        createPattern.addActionListener(new CreatePatternActionListener());
        clearButton.addActionListener(new ClearListener());
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
}
