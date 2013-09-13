package org.xendan.logmonitor.parser;

import org.xendan.logmonitor.dao.LogEntryDao;
import org.xendan.logmonitor.idea.LogMonitorPanel;
import org.xendan.logmonitor.model.LogEntry;
import org.xendan.logmonitor.model.LogSettings;
import org.xendan.logmonitor.read.LogDownloader;

import java.util.TimerTask;

/**
 * User: id967161
 * Date: 09/09/13
 */
public class DownloadAndParse extends TimerTask {
    private final LogEntryDao logEntryDao;
    private final LogMonitorPanel panel;
    private final String logPattern;
    private final LogSettings logSettings;

    public DownloadAndParse(String logPattern, LogSettings logSettings, LogEntryDao logEntryDao, LogMonitorPanel panel) {
        this.logPattern = logPattern;
        this.logSettings = logSettings;
        this.logEntryDao = logEntryDao;
        this.panel = panel;
    }

    @Override
    public void run() {
        DateParser dateParser = new DateParser();
        LogEntry lastEntry = logEntryDao.getLastEntry(logSettings);
        String lastRead = lastEntry == null ? null : dateParser.getDateAsString(logPattern, lastEntry.getDate());
        String logFile = logSettings.getServer() == null ? logSettings.getPath() : new LogDownloader(logSettings).downloadToLocal(lastRead);
        logEntryDao.addEntries(new LogFileParser(logFile, logPattern, logSettings.getMatchConfigs()).getEntries());
        panel.refresh();
    }
}
