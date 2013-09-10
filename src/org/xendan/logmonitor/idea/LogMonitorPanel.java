package org.xendan.logmonitor.idea;

import javax.swing.*;

/**
 * User: id967161
 * Date: 05/09/13
 */
public class LogMonitorPanel {
    public JPanel contentPanel;
    private JTree logTree;
    private LogMonitorPanelModel model;

    public LogMonitorPanel(LogMonitorPanelModel model) {
        this.model = model;
    }

    public void refresh() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                logTree.setModel(model.getTreeModel());
            }
        });
    }
}
