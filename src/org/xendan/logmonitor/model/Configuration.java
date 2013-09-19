package org.xendan.logmonitor.model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * User: id967161
 * Date: 03/09/13
 */
@Entity
public class Configuration extends BaseObject {
    private String projectName;
    private String logPattern;
    private List<Environment> environments = new ArrayList<Environment>();

    @OneToMany(cascade = CascadeType.ALL)
    public List<Environment> getEnvironments() {
        return environments;
    }

    public void setEnvironments(List<Environment> environments) {
        this.environments = environments;
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
        if (!super.equals(o)) return false;

        Configuration that = (Configuration) o;

        if (logPattern != null ? !logPattern.equals(that.logPattern) : that.logPattern != null) return false;
        //new array because persistentbag is compared by identity
        if (environments != null ? !new ArrayList<Environment>(environments).equals(new ArrayList<Environment>(that.environments)) : that.environments != null) return false;
        return !(projectName != null ? !projectName.equals(that.projectName) : that.projectName != null);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (projectName != null ? projectName.hashCode() : 0);
        result = 31 * result + (logPattern != null ? logPattern.hashCode() : 0);
        result = 31 * result + (environments != null ? environments.hashCode() : 0);
        return result;
    }

    public String getLogPattern() {
        return logPattern;
    }

    public void setLogPattern(String logPattern) {
        this.logPattern = logPattern;
    }
}
