package org.xendan.logmonitor.parser;

import org.apache.log4j.Logger;
import org.xendan.logmonitor.HomeResolver;
import org.xendan.logmonitor.dao.Callback;
import org.xendan.logmonitor.dao.LogService;
import org.xendan.logmonitor.idea.LogMonitorPanel;
import org.xendan.logmonitor.model.Environment;
import org.xendan.logmonitor.model.LogEntry;
import org.xendan.logmonitor.read.command.FileLoadState;
import org.xendan.logmonitor.read.command.LogDownloader;

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
    private FileLoadState monitor;
//    private static final Logger logger = LoggerFactory.getInstance().getLoggerInstance(TimerTask.class.getCanonicalName());

    private static final Logger logger = Logger.getLogger(LogMonitorPanel.class);

    public DownloadAndParse(String logPattern, Environment environment, LogService service, HomeResolver homeResolver, String project, FileLoadState monitor) {
        this.logPattern = logPattern;
        this.environment = environment;
        this.service = service;
        this.homeResolver = homeResolver;
        this.project = project;
        this.monitor = monitor;
    }

    @Override
    public void run() {
        String lastRead = environment.getLastUpdate() == null ? null : new DateParser().getDateAsString(logPattern, environment.getLastUpdate());
        List<LogEntry> entries = new LogFileReader(getLogFile(lastRead), logPattern, environment).getEntries();
        if (entries == null) {
            monitor.setEntriesNotFound();
            return;
        }
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
    }

    private String getLogFile(String lastRead) {
        return new LogDownloader(environment, homeResolver, project, monitor).downloadToLocal(lastRead);
    }
}
