package org.xendan.logmonitor.idea;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.impl.ContentImpl;
import org.xendan.logmonitor.HomeResolver;
import org.xendan.logmonitor.dao.LogService;
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
    private final LogService dao;
    private final ReaderScheduler scheduler;
    private final Serializer serializer;
    private final HomeResolver homeResolver;

    public LogMonitorToolWindowFactory(EntryAddedListener listener, LogService dao, ReaderScheduler scheduler, Serializer serializer, HomeResolver homeResolver) {
        this.listener = listener;
        this.dao = dao;
        this.scheduler = scheduler;
        this.serializer = serializer;
        this.homeResolver = homeResolver;
    }

    @Override
    public void createToolWindowContent(Project project, ToolWindow toolWindow) {
        LogMonitorPanel panel = new LogMonitorPanel(
                new LogMonitorPanelModel(project, dao, serializer, listener, homeResolver),
                project,
                new LogMonitorSettingsConfigurable(project, dao, serializer, scheduler, homeResolver));
        listener.setLogMonitorPanel(panel);
        toolWindow.getContentManager().addContent(new ContentImpl(panel.contentPanel, "", true));
        scheduler.refresh();
    }
}
