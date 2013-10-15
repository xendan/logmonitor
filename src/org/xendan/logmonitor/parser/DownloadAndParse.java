package org.xendan.logmonitor.parser;

import org.joda.time.LocalDateTime;
import org.xendan.logmonitor.HomeResolver;
import org.xendan.logmonitor.model.Environment;
import org.xendan.logmonitor.model.LogEntry;
import org.xendan.logmonitor.read.command.LogDownloader;
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
    private final HomeResolver homeResolver;
    private final String project;

    public DownloadAndParse(String logPattern, Environment environment, LogService logService, EntryAddedListener listener, HomeResolver homeResolver, String project) {
        this.logPattern = logPattern;
        this.environment = environment;
        this.logService = logService;
        this.listener = listener;
        this.homeResolver = homeResolver;
        this.project = project;
    }

    @Override
    public void run() {
        DateParser dateParser = new DateParser();
        LogEntry lastEntry = logService.getLastEntry(environment);
        LocalDateTime since = lastEntry == null ? null : lastEntry.getDate();
        String lastRead = since == null ? null : dateParser.getDateAsString(logPattern, since);
        logService.addEntries(new LogFileReader(since, getLogFile(lastRead), logPattern, environment).getEntries());
        listener.onEntriesAdded(environment);
    }

    private String getLogFile(String lastRead) {
        if (environment.getServer() == null) {
            return environment.getPath();
        }
        return new LogDownloader(environment, homeResolver).downloadToLocal(lastRead, project, environment.getName());
    }
}
