package org.xendan.logmonitor.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * User: id967161
 * Date: 03/09/13
 */
public class LogMonitorConfiguration implements Serializable {
    private String projectName;
    private List<ServerSettings> serverSettings = new ArrayList<ServerSettings>();

    public List<ServerSettings> getServerSettings() {
        return serverSettings;
    }

    public void setServerSettings(List<ServerSettings> serverSettings) {
        this.serverSettings = serverSettings;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LogMonitorConfiguration that = (LogMonitorConfiguration) o;

        return !(serverSettings != null ? !serverSettings.equals(that.serverSettings) : that.serverSettings != null);

    }

    @Override
    public int hashCode() {
        return serverSettings != null ? serverSettings.hashCode() : 0;
    }


}
