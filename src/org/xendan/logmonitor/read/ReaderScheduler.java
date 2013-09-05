package org.xendan.logmonitor.read;

import com.intellij.openapi.components.ServiceManager;
import org.xendan.logmonitor.dao.LogMonitorSettingsDao;
import org.xendan.logmonitor.model.LogMonitorConfiguration;
import org.xendan.logmonitor.model.ServerSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * User: id967161
 * Date: 05/09/13
 */
public class ReaderScheduler {

    private final LogMonitorSettingsDao dao;
    private Timer timer = new Timer();
    private boolean inited;

    public ReaderScheduler() {
        this(ServiceManager.getService(LogMonitorSettingsDao.class));
    }

    public ReaderScheduler(LogMonitorSettingsDao dao) {
        this.dao = dao;
    }

    public void reload() {
        timer.cancel();
        timer = new Timer();
        List<LogMonitorConfiguration> configs = dao.getConfig();
        List<ScpDownloader> downloaders = new ArrayList<ScpDownloader>();
        for (LogMonitorConfiguration config : configs) {
            for (ServerSettings settings : config.getServerSettings()) {
                downloaders.add(new ScpDownloader(settings.getHost(),  settings.getLogin(), settings.getPassword(), settings.getPath()));
            }
        }
        timer.scheduleAtFixedRate(new ReadAll(downloaders), 0, 10 * 60 * 1000);
        inited = true;
    }

    public void refresh() {
        if (!inited) {
            reload();
        }
    }

    private class ReadAll extends TimerTask {
        private final List<ScpDownloader> downloaders;

        public ReadAll(List<ScpDownloader> downloaders) {
            this.downloaders = downloaders;
        }

        @Override
        public void run() {
            for (ScpDownloader downloader : downloaders) {
                downloader.downloadToLocal();
            }
        }
    }
}
