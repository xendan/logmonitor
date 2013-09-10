package org.xendan.logmonitor.read;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import org.xendan.logmonitor.dao.LogEntryDao;
import org.xendan.logmonitor.dao.LogMonitorSettingsDao;
import org.xendan.logmonitor.idea.LogMonitorPanel;
import org.xendan.logmonitor.idea.LogMonitorPanelModel;
import org.xendan.logmonitor.parser.DownloadAndParse;

import java.util.Timer;

/**
 * User: id967161
 * Date: 05/09/13
 */
public class ReaderScheduler {

    private final LogMonitorSettingsDao dao;
    private final LogEntryDao logEntryDao;
    private Timer timer = new Timer();
    private boolean inited;
    private final LogMonitorPanel logMonitorPanel;

    public ReaderScheduler() {
        this(
                ServiceManager.getService(LogMonitorSettingsDao.class),
                ServiceManager.getService(LogEntryDao.class),
                new LogMonitorPanel(new LogMonitorPanelModel(
                        ServiceManager.getService(LogEntryDao.class),
                        ServiceManager.getService(LogMonitorSettingsDao.class)
                ))
        );
    }

    public ReaderScheduler(LogMonitorSettingsDao dao, LogEntryDao logEntryDao, LogMonitorPanel logMonitorPanel) {
        this.dao = dao;
        this.logEntryDao = logEntryDao;
        this.logMonitorPanel = logMonitorPanel;
    }

    public void reload() {
        timer.cancel();
        timer = new Timer();
        timer.scheduleAtFixedRate(new DownloadAndParse(dao.getConfigs(), logEntryDao, logMonitorPanel), 0, 10 * 60 * 1000);
        inited = true;
    }

    public void refresh() {
        if (!inited) {
            reload();
        }
    }

    public LogMonitorPanel getLogMonitorPanel(Project project) {
        logMonitorPanel.setProject(project);
        return logMonitorPanel;
    }
}
