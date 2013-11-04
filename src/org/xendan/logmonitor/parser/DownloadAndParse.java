package org.xendan.logmonitor.parser;

import com.intellij.idea.LoggerFactory;
import com.intellij.openapi.diagnostic.Logger;
import org.joda.time.LocalDateTime;
import org.xendan.logmonitor.HomeResolver;
import org.xendan.logmonitor.dao.ConfigurationDao;
import org.xendan.logmonitor.model.Environment;
import org.xendan.logmonitor.model.LogEntry;
import org.xendan.logmonitor.read.command.LogDownloader;

import java.util.TimerTask;

/**
 * User: id967161
 * Date: 09/09/13
 */
public class DownloadAndParse extends TimerTask {
    private final String logPattern;
    private final Environment environment;
    private final ConfigurationDao dao;
    private final EntryAddedListener listener;
    private final HomeResolver homeResolver;
    private final String project;
    private static final Logger logger = LoggerFactory.getInstance().getLoggerInstance(TimerTask.class.getCanonicalName());

    public DownloadAndParse(String logPattern, Environment environment, ConfigurationDao dao, EntryAddedListener listener, HomeResolver homeResolver, String project) {
        this.logPattern = logPattern;
        this.environment = environment;
        this.dao = dao;
        this.listener = listener;
        this.homeResolver = homeResolver;
        this.project = project;
    }

    @Override
    public void run() {
        try {
            DateParser dateParser = new DateParser();
            LogEntry lastEntry = dao.getLastEntry(environment);
            LocalDateTime since = lastEntry == null ? null : lastEntry.getDate();
            String lastRead = since == null ? null : dateParser.getDateAsString(logPattern, since);
            dao.addEntries(new LogFileReader(since, getLogFile(lastRead), logPattern, environment).getEntries());
            listener.onEntriesAdded(environment);
        } catch (Exception e) {
            //Notifications.Bus.notify(new Notification("i don't know what is this", "Error loading ", e.getMessage(), NotificationType.ERROR));
            logger.error(e);
            listener.onError(e);
        }
    }

    private String getLogFile(String lastRead) {
        if (environment.getServer() == null) {
            return environment.getPath();
        }
        return new LogDownloader(environment, homeResolver).downloadToLocal(lastRead, project, environment.getName());
    }
}
