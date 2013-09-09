package org.xendan.logmonitor.parser;

import org.xendan.logmonitor.dao.LogEntryDao;
import org.xendan.logmonitor.model.LogEntry;
import org.xendan.logmonitor.model.LogMonitorConfiguration;
import org.xendan.logmonitor.model.Matchers;
import org.xendan.logmonitor.model.ServerSettings;
import org.xendan.logmonitor.read.LogDownloader;
import org.xendan.logmonitor.read.MatcherService;

import java.util.List;
import java.util.Map;
import java.util.TimerTask;

/**
 * User: id967161
 * Date: 09/09/13
 */
public class DownloadAndParse extends TimerTask {
    private final List<LogMonitorConfiguration> configs;
    private final LogEntryDao logEntryDao;
    private final MatcherService matcherService;

    public DownloadAndParse(List<LogMonitorConfiguration> configs, LogEntryDao logEntryDao, MatcherService matcherService) {
        this.configs = configs;
        this.logEntryDao = logEntryDao;
        this.matcherService = matcherService;
    }

    @Override
    public void run() {
        for (LogMonitorConfiguration config : configs) {
            DateParser dateParser = new DateParser();
            Map<ServerSettings,Matchers> matchers = matcherService.getMatchers(config);
            for (ServerSettings settings : config.getServerSettings()) {
                LogEntry lastEntry = logEntryDao.getLastEntry(settings);
                String lastRead = lastEntry == null ? null : dateParser.getDateAsString(config.getLogPattern(), lastEntry.getDate());
                String logFile = new LogDownloader(settings).downloadToLocal(lastRead);
                logEntryDao.addEntries(new LogFileParser(logFile, config.getLogPattern(), matchers.get(settings)).getEntries(), settings);
            }
        }

    }
}
