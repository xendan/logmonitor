package org.xendan.logmonitor.read;

import org.xendan.logmonitor.HomeResolver;
import org.xendan.logmonitor.dao.DefaultCallBack;
import org.xendan.logmonitor.dao.LogService;
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

    private final LogService service;
    private final EntryAddedListener listener;
    private boolean inited;
    private HomeResolver homeResolver;
    private ScheduledThreadPoolExecutor executor;

    public ReaderScheduler(LogService service, EntryAddedListener listener, HomeResolver homeResolver) {
        this.homeResolver = homeResolver;
        this.service = service;
        this.listener = listener;
    }

    public void reload() {
        listener.beforeReload();
        if (executor != null) {
            executor.shutdownNow();
        }
        service.getConfigs(new DefaultCallBack<List<Configuration>>() {
            @Override
            public void onAnswer(List<Configuration> configs) {
                int nOfThreads = 0;
                for (Configuration config : configs) {
                    nOfThreads += config.getEnvironments().size();
                }
                if (nOfThreads > 0) {
                    executor = new ScheduledThreadPoolExecutor(nOfThreads);
                    for (Configuration configuration : configs) {
                        for (Environment environment : configuration.getEnvironments()) {
                            Runnable command = new DownloadAndParse(configuration.getLogPattern(), environment, service, listener, homeResolver,configuration.getProjectName());
                            executor.scheduleAtFixedRate(command, 0, environment.getUpdateInterval() * 60, TimeUnit.SECONDS);
                        }
                    }
                }
            }
        });
        inited = true;
    }

    public void refresh() {
        if (!inited) {
            reload();
        }
    }

}
