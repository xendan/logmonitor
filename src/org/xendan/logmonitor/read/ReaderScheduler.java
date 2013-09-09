package org.xendan.logmonitor.read;

import com.intellij.openapi.components.ServiceManager;
import org.xendan.logmonitor.dao.LogEntryDao;
import org.xendan.logmonitor.dao.LogMonitorSettingsDao;
import org.xendan.logmonitor.parser.DownloadAndParse;

import java.util.Timer;

/**
 * User: id967161
 * Date: 05/09/13
 */
public class ReaderScheduler {

    private final LogMonitorSettingsDao dao;
    private final MatcherService matcherService;
    private final LogEntryDao logEntryDao;
    private Timer timer = new Timer();
    private boolean inited;

    public ReaderScheduler() {
        this(
                ServiceManager.getService(LogMonitorSettingsDao.class),
                ServiceManager.getService(MatcherService.class),
                ServiceManager.getService(LogEntryDao.class)
        );
    }

    public ReaderScheduler(LogMonitorSettingsDao dao, MatcherService matcherService, LogEntryDao logEntryDao) {
        this.dao = dao;
        this.matcherService = matcherService;
        this.logEntryDao = logEntryDao;
    }

    public void reload() {
        timer.cancel();
        timer = new Timer();
        timer.scheduleAtFixedRate(new DownloadAndParse(dao.getConfig(), logEntryDao, matcherService), 0, 10 * 60 * 1000);
        inited = true;
    }

    public void refresh() {
        if (!inited) {
            reload();
        }
    }

}
