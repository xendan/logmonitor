package org.xendan.logmonitor.parser;

import org.xendan.logmonitor.dao.DefaultCallBack;
import org.xendan.logmonitor.idea.LogMonitorPanel;
import org.xendan.logmonitor.model.Environment;

import javax.swing.*;

/**
 * User: id967161
 * Date: 20/09/13
 */
public class EntryStatusListener {

    private LogMonitorPanel logMonitorPanel;

    public void setLogMonitorPanel(LogMonitorPanel logMonitorPanel) {
        this.logMonitorPanel = logMonitorPanel;
    }

    public void onEntriesAdded(final Environment environment) {
        logMonitorPanel.onEntriesAdded(environment);
    }

    public void beforeReload() {
        if (logMonitorPanel != null) {
            logMonitorPanel.initModel(DefaultCallBack.DO_NOTHING);
        }
    }

    public void setEnvironmentMessage(final Environment environment, final String message, final boolean isError) {
        swingInvoke(new Runnable() {
            @Override
            public void run() {
                logMonitorPanel.updateDownloadAndParseStatus(environment, message, isError);
            }
        });
    }

    private void swingInvoke(Runnable runnable) {
        if (logMonitorPanel != null) {
            SwingUtilities.invokeLater(runnable);
        }
    }

    public void onEntriesNotFound(final Environment environment) {
        swingInvoke(new Runnable() {
            @Override
            public void run() {
                logMonitorPanel.onEntriesNotFound(environment);
            }
        });
    }
}
