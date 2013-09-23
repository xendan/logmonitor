package org.xendan.logmonitor.parser;

import org.joda.time.LocalDateTime;
import org.xendan.logmonitor.model.Environment;
import org.xendan.logmonitor.model.LogEntry;
import org.xendan.logmonitor.read.LogDownloader;
import org.xendan.logmonitor.service.LogService;

import java.util.TimerTask;

/**
 * User: id967161
 * Date: 09/09/13
 */
public class DownloadAndParse extends TimerTask {
    private final String logPattern;
    private final Environment environment;
    private final LogService logService;
    private final EntryAddedListener listener;

    public DownloadAndParse(String logPattern, Environment environment, LogService logService, EntryAddedListener listener) {
        this.logPattern = logPattern;
        this.environment = environment;
        this.logService = logService;
        this.listener = listener;
    }

    @Override
    public void run() {
        DateParser dateParser = new DateParser();
        LogEntry lastEntry = logService.getLastEntry(environment);
        LocalDateTime since = lastEntry == null ? null : lastEntry.getDate();
        String lastRead = since == null ? null : dateParser.getDateAsString(logPattern, since);
        String logFile = environment.getServer() == null ? environment.getPath() : new LogDownloader(environment).downloadToLocal(lastRead);
        logService.addEntries(new LogFileParser(since, logFile, logPattern, environment.getMatchConfigs()).getEntries());
        listener.onEntriesAdded(environment);
    }
}
