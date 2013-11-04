package org.xendan.logmonitor.parser;

import org.xendan.logmonitor.idea.LogMonitorPanel;
import org.xendan.logmonitor.model.Environment;

import javax.swing.*;

/**
 * User: id967161
 * Date: 20/09/13
 */
public class EntryAddedListener {

    private LogMonitorPanel logMonitorPanel;

    public void setLogMonitorPanel(LogMonitorPanel logMonitorPanel) {
        this.logMonitorPanel = logMonitorPanel;
    }

    public void onEntriesAdded(final Environment environment) {
        if (logMonitorPanel != null) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    logMonitorPanel.onEntriesAdded(environment);
                }
            });
        }
    }

    public void onError(final Exception e) {
        if (logMonitorPanel != null) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    logMonitorPanel.onException(e);
                }
            });
        }
    }

    public void beforeReload() {
        if (logMonitorPanel != null) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    logMonitorPanel.initModel();
                }
            });
        }
    }
}
