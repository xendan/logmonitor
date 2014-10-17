package org.xendan.logmonitor.idea;

import com.intellij.openapi.components.ApplicationComponent;
import org.jetbrains.annotations.NotNull;
import org.mortbay.jetty.plugin.Jetty6RunWar;
import org.xendan.logmonitor.idea.read.Settings;

public class LogmonitorApplicationComponent implements ApplicationComponent {

	public LogmonitorApplicationComponent(Settings settings) {
		if (settings.getState() != null && settings.getState().getUseBuiltInServer() != null) {
			//new JettyRunner("", "", 123);
			Jetty6RunWar runner = new Jetty6RunWar();
			try {
				runner.execute();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void initComponent() {

	}

	@Override
	public void disposeComponent() {

	}

	@NotNull
	@Override
	public String getComponentName() {
		return "Logmonitor component";
	}
}
