package org.xendan.logmonitor.parser;

import org.xendan.logmonitor.dao.LogEntryDao;
import org.xendan.logmonitor.idea.LogMonitorPanel;
import org.xendan.logmonitor.model.LogEntry;
import org.xendan.logmonitor.model.LogMonitorConfiguration;
import org.xendan.logmonitor.model.ServerSettings;
import org.xendan.logmonitor.read.LogDownloader;

import java.util.List;
import java.util.TimerTask;

/**
 * User: id967161
 * Date: 09/09/13
 */
public class DownloadAndParse extends TimerTask {
    private final List<LogMonitorConfiguration> configs;
    private final LogEntryDao logEntryDao;
    private final LogMonitorPanel panel;

    public DownloadAndParse(List<LogMonitorConfiguration> configs, LogEntryDao logEntryDao, LogMonitorPanel panel) {
        this.configs = configs;
        this.logEntryDao = logEntryDao;
        this.panel = panel;
    }

    @Override
    public void run() {
        for (LogMonitorConfiguration config : configs) {
            DateParser dateParser = new DateParser();
            for (ServerSettings settings : config.getServerSettings()) {
                LogEntry lastEntry = logEntryDao.getLastEntry(settings);
                String lastRead = lastEntry == null ? null : dateParser.getDateAsString(config.getLogPattern(), lastEntry.getDate());
                String logFile = new LogDownloader(settings).downloadToLocal(lastRead);
                logEntryDao.addEntries(new LogFileParser(logFile, config.getLogPattern(), settings.getMatchConfigs()).getEntries());
            }
        }
        panel.refresh();
    }
}
