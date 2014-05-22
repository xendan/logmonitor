package org.xendan.logmonitor.idea.components;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import org.xendan.logmonitor.HomeResolver;
import org.xendan.logmonitor.idea.read.Settings;
import org.xendan.logmonitor.util.Serializer;

/**
 * User: id967161
 * Date: 05/09/13
 */
public class LogMonitorToolWindowFactory implements ToolWindowFactory {
    private final Settings settings;
    private final Serializer serializer;
    private final HomeResolver homeResolver;
    public LogMonitorToolWindowFactory(Settings settings, Serializer serializer, HomeResolver homeResolver) {
        this.settings = settings;
        this.serializer = serializer;
        this.homeResolver = homeResolver;
    }

    @Override
    public void createToolWindowContent(Project project, ToolWindow toolWindow) {

        //LogMonitorSettingsConfigurable(Settings settings, Serializer serializer, HomeResolver homeResolver) {
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
