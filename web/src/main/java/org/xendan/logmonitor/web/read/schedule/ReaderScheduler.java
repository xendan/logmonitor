package org.xendan.logmonitor.web.read.schedule;

import com.google.inject.Inject;
import org.xendan.logmonitor.HomeResolver;
import org.xendan.logmonitor.model.Configuration;
import org.xendan.logmonitor.model.Environment;
import org.xendan.logmonitor.web.service.LogService;

import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * User: id967161
 * Date: 05/09/13
 */
public class ReaderScheduler {

    //    private boolean inited;
    private HomeResolver homeResolver;
    private LogService service;
    private ScheduledThreadPoolExecutor executor;

    @Inject
    public ReaderScheduler(HomeResolver homeResolver, LogService service) {
        this.homeResolver = homeResolver;
        this.service = service;
    }

    public void reload() {
        if (executor != null) {
            executor.shutdownNow();
        }
        int nOfEnvironments = 0;
        List<Configuration> configs = service.getConfigs();
        for (Configuration config : configs) {
            nOfEnvironments += config.getEnvironments().size();
        }
        if (nOfEnvironments > 0) {
            executor = new ScheduledThreadPoolExecutor(nOfEnvironments);
            for (Configuration configuration : configs) {
                for (Environment environment : configuration.getEnvironments()) {
                    executor.scheduleAtFixedRate(createCommand(configuration, environment), 0, environment.getUpdateInterval() * 60, TimeUnit.SECONDS);
                }
            }
        }
    }

    public void refreshSynchronously(Long envId) {
        Configuration configuration = service.getConfigByEnvironment(envId);
        for (Environment environment : configuration.getEnvironments()) {
            if (environment.getId().equals(envId)) {
                createCommand(configuration, environment).run();
                return;
            }
        }
    }

    private Runnable createCommand(Configuration configuration, Environment environment) {
        return new DownloadAndParse(configuration.getLogPattern(), environment, service, homeResolver, configuration.getProjectName());
    }
}
