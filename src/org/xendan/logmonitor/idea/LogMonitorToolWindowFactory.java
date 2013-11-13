package org.xendan.logmonitor.idea;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.impl.ContentImpl;
import org.xendan.logmonitor.dao.ConfigurationDao;
import org.xendan.logmonitor.idea.model.LogMonitorPanelModel;
import org.xendan.logmonitor.parser.EntryAddedListener;
import org.xendan.logmonitor.read.ReaderScheduler;
import org.xendan.logmonitor.read.Serializer;

/**
 * User: id967161
 * Date: 05/09/13
 */
public class LogMonitorToolWindowFactory implements ToolWindowFactory {
    private final EntryAddedListener listener;
    private final ConfigurationDao dao;
    private final ReaderScheduler scheduler;
    private final Serializer serializer;

    public LogMonitorToolWindowFactory(EntryAddedListener listener, ConfigurationDao dao, ReaderScheduler scheduler, Serializer serializer) {
        this.listener = listener;
        this.dao = dao;
        this.scheduler = scheduler;
        this.serializer = serializer;
    }

    @Override
    public void createToolWindowContent(Project project, ToolWindow toolWindow) {
        LogMonitorPanel panel = new LogMonitorPanel(new LogMonitorPanelModel(dao, serializer, listener), project, new LogMonitorSettingsConfigurable(project, dao, serializer, scheduler));
        listener.setLogMonitorPanel(panel);
        toolWindow.getContentManager().addContent(new ContentImpl(panel.contentPanel, "", true));
        scheduler.refresh();
    }
}
