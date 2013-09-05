package org.xendan.logmonitor.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * User: id967161
 * Date: 03/09/13
 */
@Entity
public class LogMonitorConfiguration implements Serializable {
    private Long id;
    private String projectName;
    private List<ServerSettings> serverSettings = new ArrayList<ServerSettings>();

    @Id
    @GeneratedValue
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
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
