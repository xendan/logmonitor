package org.xendan.logmonitor.read;

import org.xendan.logmonitor.HomeResolver;
import org.xendan.logmonitor.dao.DefaultCallBack;
import org.xendan.logmonitor.dao.LogService;
import org.xendan.logmonitor.model.Configuration;
import org.xendan.logmonitor.model.Environment;
import org.xendan.logmonitor.parser.DownloadAndParse;
import org.xendan.logmonitor.parser.EntryStatusListener;
import org.xendan.logmonitor.read.command.FileLoadState;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * User: id967161
 * Date: 05/09/13
 */
public class ReaderScheduler {

    private final LogService service;
    private final EntryStatusListener listener;
    private boolean inited;
    private HomeResolver homeResolver;
    private ScheduledThreadPoolExecutor executor;

    public ReaderScheduler(LogService service, EntryStatusListener listener, HomeResolver homeResolver) {
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
                int nOfEnvironments = 0;
                for (Configuration config : configs) {
                    nOfEnvironments += config.getEnvironments().size();
                }
                if (nOfEnvironments > 0) {
                    executor = new ScheduledThreadPoolExecutor(nOfEnvironments + 1);
                    Map<Environment, FileLoadState> loadingStates = new HashMap<Environment, FileLoadState>();
                    for (Configuration configuration : configs) {
                        for (Environment environment : configuration.getEnvironments()) {
                            loadingStates.put(environment, new FileLoadState());
                            Runnable command = new DownloadAndParse(configuration.getLogPattern(), environment, service, homeResolver,configuration.getProjectName(), loadingStates.get(environment));
                            executor.scheduleAtFixedRate(command, 0, environment.getUpdateInterval() * 60, TimeUnit.SECONDS);
                        }
                    }
                    executor.scheduleAtFixedRate(new EnvironmentStateMonitor(loadingStates, listener), 1, 3, TimeUnit.SECONDS);
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
