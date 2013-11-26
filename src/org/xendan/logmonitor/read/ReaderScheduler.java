package org.xendan.logmonitor.read;

import org.xendan.logmonitor.HomeResolver;
import org.xendan.logmonitor.dao.ConfigurationDao;
import org.xendan.logmonitor.model.Configuration;
import org.xendan.logmonitor.model.Environment;
import org.xendan.logmonitor.parser.DownloadAndParse;
import org.xendan.logmonitor.parser.EntryAddedListener;

import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * User: id967161
 * Date: 05/09/13
 */
public class ReaderScheduler {

    private final ConfigurationDao dao;
    private final EntryAddedListener listener;
    private boolean inited;
    private HomeResolver homeResolver;
    private ScheduledThreadPoolExecutor executor;

    public ReaderScheduler(ConfigurationDao dao, EntryAddedListener listener, HomeResolver homeResolver) {
        this.homeResolver = homeResolver;
        this.dao = dao;
        this.listener = listener;
    }

    public void reload() {
        listener.beforeReload();
        if (executor != null) {
            executor.shutdownNow();
        }
        new Thread() {
            @Override
            public void run() {
                List<Configuration> configs = dao.getConfigs();
                int nOfThreads = 0;
                for (Configuration config : configs) {
                    nOfThreads += config.getEnvironments().size();
                }
                if (nOfThreads > 0) {
                    executor = new ScheduledThreadPoolExecutor(nOfThreads);
                    for (Configuration configuration : dao.getConfigs()) {
                        for (Environment environment : configuration.getEnvironments()) {
                            Runnable command = new DownloadAndParse(configuration.getLogPattern(), environment, dao, listener, homeResolver,configuration.getProjectName());
                            executor.scheduleAtFixedRate(command, 3 * Math.round(Math.random()), environment.getUpdateInterval() * 60, TimeUnit.SECONDS);
                        }
                    }
                }
            }
        }.start();
        inited = true;
    }

    public void refresh() {
        if (!inited) {
            reload();
        }
    }

}
