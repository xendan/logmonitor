package org.xendan.logmonitor.web.read.schedule;

import org.apache.log4j.Logger;
import org.xendan.logmonitor.HomeResolver;
import org.xendan.logmonitor.model.Environment;
import org.xendan.logmonitor.model.EnvironmentStatus;
import org.xendan.logmonitor.model.LogEntry;
import org.xendan.logmonitor.web.read.command.LogDownloader;
import org.xendan.logmonitor.web.read.parse.DateParser;
import org.xendan.logmonitor.web.read.parse.LogFileReader;
import org.xendan.logmonitor.web.service.LogService;

import java.util.List;

/**
 * User: id967161
 * Date: 09/09/13
 */
public class DownloadAndParse implements Runnable {
    private final String logPattern;
    private final Environment environment;
    private final LogService service;
    private final HomeResolver homeResolver;
    private final String project;

    private static final Logger logger = Logger.getLogger(DownloadAndParse.class);

    public DownloadAndParse(String logPattern, Environment environment, LogService service, HomeResolver homeResolver, String project) {
        this.logPattern = logPattern;
        this.environment = environment;
        this.service = service;
        this.homeResolver = homeResolver;
        this.project = project;
    }

    @Override
    public void run() {
        String lastRead = environment.getLastUpdate() == null ? null : new DateParser().getDateAsString(logPattern, environment.getLastUpdate());
        service.setEnvironmentStatus(environment, EnvironmentStatus.DOWNLOADING);
        String logFile = getLogFile(lastRead);
        service.setEnvironmentStatus(environment, EnvironmentStatus.PARSING);
        List<LogEntry> entries = new LogFileReader(logFile, logPattern, environment).getEntries();
        if (entries == null) {
            service.setEnvironmentStatus(environment, EnvironmentStatus.NO_ENTIRIES_FOUND);
            return;
        }
        service.addEntries(entries);
        service.setEnvironmentStatus(environment, EnvironmentStatus.WAITING);
        /*
        monitor.onParseEntriesStarted();
        service.addEntries(entries, new Callback<Void>(){
            @Override
            public void onAnswer(Void answer) {
                monitor.onEntriesAdded();
            }

            @Override
            public void onFail(Throwable error) {
                monitor.onAddEntriesError(error);
                logger.error(error);
            }
        });
        */
    }

    private String getLogFile(String lastRead) {
        return new LogDownloader(environment, homeResolver, project, service).downloadToLocal(lastRead);
    }
}
