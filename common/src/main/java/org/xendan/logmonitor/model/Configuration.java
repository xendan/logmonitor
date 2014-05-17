package org.xendan.logmonitor.model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * User: id967161
 * Date: 03/09/13
 */
@Entity
@XmlRootElement
public class Configuration extends BaseObject {
    public static final String ENVIRONMENTS = "environments";
    private String projectName;
    private String logPattern;
    private List<Environment> environments = new ArrayList<Environment>();
    private List<String> visibleFields = new ArrayList<String>();

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

    public String getLogPattern() {
        return logPattern;
    }

    public void setLogPattern(String logPattern) {
        this.logPattern = logPattern;
    }

    @Transient
    public List<String> getVisibleFields() {
        return visibleFields;
    }

    public void setVisibleFields(List<String> visibleFields) {
        this.visibleFields = visibleFields;
    }

    @Override
    public String toString() {
        return projectName;
    }
}
