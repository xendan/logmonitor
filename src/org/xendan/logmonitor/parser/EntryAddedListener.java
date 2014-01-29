package org.xendan.logmonitor.parser;

import org.joda.time.LocalDateTime;
import org.xendan.logmonitor.dao.DefaultCallBack;
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

    public void onEntriesAdded(final Environment environment, final LocalDateTime since) {
        logMonitorPanel.onEntriesAdded(environment, since);
    }

    public void onError(final Throwable e) {
        if (logMonitorPanel != null) {
            logMonitorPanel.onException(e);
        }
    }

    public void beforeReload() {
        if (logMonitorPanel != null) {
            logMonitorPanel.initModel(DefaultCallBack.DO_NOTHING);
        }
    }
}
