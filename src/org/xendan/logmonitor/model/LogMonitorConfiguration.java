package org.xendan.logmonitor.model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * User: id967161
 * Date: 03/09/13
 */
@Entity
public class LogMonitorConfiguration extends BaseObject {
    private String projectName;
    private String logPattern;
    private List<LogSettings> logSettings = new ArrayList<LogSettings>();

    @OneToMany(cascade = CascadeType.ALL)
    public List<LogSettings> getLogSettings() {
        return logSettings;
    }

    public void setLogSettings(List<LogSettings> logSettings) {
        this.logSettings = logSettings;
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

        return !(logSettings != null ? !logSettings.equals(that.logSettings) : that.logSettings != null);

    }

    @Override
    public int hashCode() {
        return logSettings != null ? logSettings.hashCode() : 0;
    }

    public String getLogPattern() {
        return logPattern;
    }

    public void setLogPattern(String logPattern) {
        this.logPattern = logPattern;
    }
}
