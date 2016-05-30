package org.xendan.logmonitor.web.read.schedule;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.xendan.logmonitor.HomeResolver;
import org.xendan.logmonitor.model.Environment;
import org.xendan.logmonitor.model.LogEntry;
import org.xendan.logmonitor.web.service.EnvironmentMessage;
import org.xendan.logmonitor.web.read.command.LogDownloader;
import org.xendan.logmonitor.web.read.parse.DateParser;
import org.xendan.logmonitor.web.read.parse.LogFileReader;
import org.xendan.logmonitor.web.service.LogService;

import java.io.File;
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
    private int counter = 0;

    public DownloadAndParse(String logPattern, Environment environment, LogService service, HomeResolver homeResolver, String project) {
        this.logPattern = logPattern;
        this.environment = environment;
        this.service = service;
        this.homeResolver = homeResolver;
        this.project = project;
    }

    @Override
    public void run() {
        try {
            String lastRead = environment.getLastUpdate() == null ? null : new DateParser().getDateAsString(logPattern, environment.getLastUpdate());
            String logFile = getLogFile(lastRead);
            if (logFile == null) {
                return;
            }
            File file = new File(logFile);
            if (!file.exists()) {
                service.setEnvironmentStatus(environment, EnvironmentMessage.FILE_NOT_FOUND);
                return;
            }
            service.setEnvironmentStatus(environment, EnvironmentMessage.PARSING);
            List<LogEntry> entries = new LogFileReader(logFile, logPattern, environment).getEntries();
            if (entries == null) {
                service.setEnvironmentStatus(environment, EnvironmentMessage.NO_ENTRIES_FOUND);
                return;
            }
            service.merge(environment);
            service.addEntries(entries);
            service.setEnvironmentStatus(environment, EnvironmentMessage.WAITING);
        } catch (Exception e) {
            logger.error(e);
        }
    }

    private String getLogFile(String lastRead) {
        return new LogDownloader(environment, homeResolver, project, service).downloadToLocal(lastRead);
    }
}
