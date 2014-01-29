package org.xendan.logmonitor.model;

import org.apache.log4j.Level;

import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * User: kcyxa
 * Date: 1/26/13
 */
@Entity
public class MatchConfig extends BaseObject implements Comparable<MatchConfig> {
    private String level = Level.DEBUG.toString();
    private boolean general;
    private boolean showNotification;
    private String name;
    private String message;
    private Integer weight;

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Column(columnDefinition="text")
    public String getMessage() {
        return message;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    public boolean isGeneral() {
        return general;
    }

    public void setGeneral(boolean general) {
        this.general = general;
    }

    @Override
    public int compareTo(MatchConfig o) {
        int otherWeight = o.getWeight() == null ? 0 : o.getWeight();
        int weight = getWeight() == null ? 0 : getWeight();
        return otherWeight - weight;
    }

    public boolean isShowNotification() {
        return showNotification;
    }

    public void setShowNotification(boolean showNotification) {
        this.showNotification = showNotification;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        MatchConfig that = (MatchConfig) o;

        if (showNotification != that.showNotification) return false;
        if (general != that.general) return false;
        if (level != null ? !level.equals(that.level) : that.level != null) return false;
        if (message != null ? !message.equals(that.message) : that.message != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        return !(weight != null ? !weight.equals(that.weight) : that.weight != null);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (level != null ? level.hashCode() : 0);
        result = 31 * result + (general ? 1 : 0);
        result = 31 * result + (showNotification ? 1 : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (message != null ? message.hashCode() : 0);
        result = 31 * result + (weight != null ? weight.hashCode() : 0);
        return result;
    }


}
