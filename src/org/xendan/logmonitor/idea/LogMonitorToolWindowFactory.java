package org.xendan.logmonitor.idea;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.impl.ContentImpl;
import org.xendan.logmonitor.read.ReaderScheduler;

/**
 * User: id967161
 * Date: 05/09/13
 */
public class LogMonitorToolWindowFactory implements ToolWindowFactory {
    @Override
    public void createToolWindowContent(Project project, ToolWindow toolWindow) {
        ServiceManager.getService(ReaderScheduler.class).refresh();
        toolWindow.getContentManager().addContent(new ContentImpl(new LogMonitorPanel().contentPanel, "AAA", true));
    }
}
