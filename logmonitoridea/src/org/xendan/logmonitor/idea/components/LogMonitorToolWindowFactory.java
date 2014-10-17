package org.xendan.logmonitor.idea.components;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.impl.ContentImpl;
import org.xendan.logmonitor.HomeResolver;
import org.xendan.logmonitor.idea.OnOkAction;
import org.xendan.logmonitor.idea.read.Settings;
import org.xendan.logmonitor.util.Serializer;

import javax.swing.*;

/**
 * User: id967161
 * Date: 05/09/13
 */
public class LogMonitorToolWindowFactory implements ToolWindowFactory {
	private final Settings settings;
	private final JComponent configurableComponent;

	public LogMonitorToolWindowFactory(Settings settings, Serializer serializer, HomeResolver homeResolver) {
		this.settings = settings;
		configurableComponent = new LogMonitorSettingsConfigurable(settings, serializer, homeResolver).createComponent();
	}

	@Override
	public void createToolWindowContent(Project project, ToolWindow toolWindow) {
		toolWindow.getContentManager().addContent(new ContentImpl(createContentComponent(), "", true));
	}

	private JComponent createContentComponent() {
		if (settings.isUpAndRunning()) {
			new EnvironmentErrorTree().getContent();
		}
		return buildConfigurationLink(settings.getUrl());
	}

	private JComponent buildConfigurationLink(String url) {
		return new HyperTextLink(getLinkText(url), new Runnable(){
			@Override
			public void run() {
				new BaseDialog(OnOkAction.DO_NOTHING, configurableComponent).setTitleAndShow("Logmonitor configuration");
			}
		});
	}

	private String getLinkText(String url) {
		String openLink = "<a href='open_config'> Configure...</a>";
		if (url == null) {
			return "No configuration found. " + openLink;
		}
		return "Url '" + url + "' is not valid." + openLink;
	}
}
