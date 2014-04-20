package org.xendan.logmonitor.idea.components;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;

/**
 * User: id967161
 * Date: 05/09/13
 */
public class LogMonitorToolWindowFactory implements ToolWindowFactory {
    /**
     * private final EntryStatusListener listener;
     * private final LogService dao;
     * private final ReaderScheduler scheduler;
     * private final Serializer serializer;
     * private final HomeResolver homeResolver;
     */
    public LogMonitorToolWindowFactory() {
    }

    @Override
    public void createToolWindowContent(Project project, ToolWindow toolWindow) {
        /**
         LogMonitorPanel panel = new LogMonitorPanel(
         new LogMonitorPanelModel(project, dao, serializer, listener, homeResolver),
         project,
         new LogMonitorSettingsConfigurable(project, dao, serializer, scheduler, homeResolver));
         listener.setLogMonitorPanel(panel);
         toolWindow.getContentManager().addContent(new ContentImpl(panel.contentPanel, "", true));
         scheduler.refresh();
         */
    }
}
