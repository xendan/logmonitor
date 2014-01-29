package org.xendan.logmonitor.parser;

import org.apache.log4j.Logger;
import org.joda.time.LocalDateTime;
import org.xendan.logmonitor.HomeResolver;
import org.xendan.logmonitor.dao.Callback;
import org.xendan.logmonitor.dao.LogService;
import org.xendan.logmonitor.idea.LogMonitorPanel;
import org.xendan.logmonitor.model.Environment;
import org.xendan.logmonitor.model.LogEntry;
import org.xendan.logmonitor.read.command.LogDownloader;

/**
 * User: id967161
 * Date: 09/09/13
 */
public class DownloadAndParse implements Runnable {
    private final String logPattern;
    private final Environment environment;
    private final LogService service;
    private final EntryAddedListener listener;
    private final HomeResolver homeResolver;
    private final String project;
//    private static final Logger logger = LoggerFactory.getInstance().getLoggerInstance(TimerTask.class.getCanonicalName());

    private static final Logger logger = Logger.getLogger(LogMonitorPanel.class);

    public DownloadAndParse(String logPattern, Environment environment, LogService service, EntryAddedListener listener, HomeResolver homeResolver, String project) {
        this.logPattern = logPattern;
        this.environment = environment;
        this.service = service;
        this.listener = listener;
        this.homeResolver = homeResolver;
        this.project = project;
    }

    @Override
    public void run() {
        try {
            service.getLastEntry(environment, new onEntryLoaded());
        } catch (Exception e) {
            handleError(e);
        }
    }

    private void handleError(Throwable e) {
        logger.error(e);
        listener.onError(e);
    }

    private String getLogFile(String lastRead) {
        return new LogDownloader(environment, homeResolver, project).downloadToLocal(lastRead);
    }

    private class onEntryLoaded implements Callback<LogEntry> {
        @Override
        public void onAnswer(LogEntry lastEntry) {
            final LocalDateTime since = lastEntry == null ? null : lastEntry.getDate();
            String lastRead = since == null ? null : new DateParser().getDateAsString(logPattern, since);
            service.addEntries(new LogFileReader(since, getLogFile(lastRead), logPattern, environment).getEntries(), new Callback<Void>(){
                @Override
                public void onAnswer(Void answer) {
                    listener.onEntriesAdded(environment, since);
                }

                @Override
                public void onFail(Throwable error) {
                    handleError(error);
                }
            });
        }

        @Override
        public void onFail(Throwable error) {
            handleError(error);
        }
    }

}
