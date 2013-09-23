package org.xendan.logmonitor.idea;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.impl.ContentImpl;
import org.xendan.logmonitor.idea.model.LogMonitorPanelModel;
import org.xendan.logmonitor.parser.EntryAddedListener;
import org.xendan.logmonitor.read.ReaderScheduler;
import org.xendan.logmonitor.read.Serializer;
import org.xendan.logmonitor.service.LogService;

/**
 * User: id967161
 * Date: 05/09/13
 */
public class LogMonitorToolWindowFactory implements ToolWindowFactory {
    private final EntryAddedListener listener;
    private final LogService service;
    private final ReaderScheduler scheduler;
    private final Serializer serializer;

    public LogMonitorToolWindowFactory(EntryAddedListener listener, LogService service, ReaderScheduler scheduler, Serializer serializer) {
        this.listener = listener;
        this.service = service;
        this.scheduler = scheduler;
        this.serializer = serializer;
    }

    @Override
    public void createToolWindowContent(Project project, ToolWindow toolWindow) {
        LogMonitorPanel panel = new LogMonitorPanel(new LogMonitorPanelModel(service), project, new LogMonitorSettingsConfigurable(project, service, serializer, scheduler));
        listener.setLogMonitorPanel(panel);
        toolWindow.getContentManager().addContent(new ContentImpl(panel.contentPanel, "", true));
        scheduler.refresh();
    }
}
