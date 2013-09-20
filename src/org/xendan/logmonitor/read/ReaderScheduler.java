package org.xendan.logmonitor.read;

import org.xendan.logmonitor.dao.ConfigurationDao;
import org.xendan.logmonitor.model.Configuration;
import org.xendan.logmonitor.model.Environment;
import org.xendan.logmonitor.parser.DownloadAndParse;
import org.xendan.logmonitor.parser.EntryAddedListener;
import org.xendan.logmonitor.service.LogService;

import java.util.Timer;

/**
 * User: id967161
 * Date: 05/09/13
 */
public class ReaderScheduler {

    private final ConfigurationDao dao;
    private final LogService logService;
    private final EntryAddedListener listener;
    private Timer timer = new Timer();
    private boolean inited;

    public ReaderScheduler(ConfigurationDao dao, LogService logService, EntryAddedListener listener) {
        this.dao = dao;
        this.logService = logService;
        this.listener = listener;
    }


    public void reload() {
        timer.cancel();
        timer = new Timer();
        for (Configuration configuration : dao.getConfigs()) {
            for (Environment environment : configuration.getEnvironments()) {
                timer.scheduleAtFixedRate(new DownloadAndParse(configuration.getLogPattern(), environment, logService, listener), 0, environment.getUpdateInterval() * 60 * 1000);
            }
        }
        inited = true;
    }

    public void refresh() {
        if (!inited) {
            reload();
        }
    }

}
