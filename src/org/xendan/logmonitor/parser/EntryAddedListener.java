package org.xendan.logmonitor.parser;

import org.xendan.logmonitor.idea.LogMonitorPanel;
import org.xendan.logmonitor.model.Environment;

/**
 * User: id967161
 * Date: 20/09/13
 */
public class EntryAddedListener {

    private LogMonitorPanel logMonitorPanel;

    public void setLogMonitorPanel(LogMonitorPanel logMonitorPanel) {
        this.logMonitorPanel = logMonitorPanel;
    }

    public void onEntriesAdded(Environment environment) {
        if (logMonitorPanel != null) {
            logMonitorPanel.onEntriesAdded(environment);
        }
    }
}
