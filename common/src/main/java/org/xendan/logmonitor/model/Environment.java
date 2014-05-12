package org.xendan.logmonitor.model;

import org.joda.time.LocalDateTime;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * User: id967161
 * Date: 03/09/13
 */
@Entity
public class Environment extends BaseObject {
//    public static final String SOON = "soon";
//    private final DateTimeFormatter HOURS_MINUTES = DateTimeFormat.forPattern("HH:mm");
    private Server server;
    private String path;
    private String name;
    /**
     * Update interval in minutes
     */
    private int updateInterval;
    private List<MatchConfig> matchConfigs = new ArrayList<MatchConfig>();
    private LocalDateTime lastUpdate;


    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    //TODO WHY Column "MATCHCONFI1_.WEIGHT" not found; SQL statement
    @ManyToMany(cascade = CascadeType.ALL)
    @OrderBy(value="weight desc")
    public List<MatchConfig> getMatchConfigs() {
        return matchConfigs;
    }

    public void setMatchConfigs(List<MatchConfig> matchConfigs) {
        this.matchConfigs = matchConfigs;
    }

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getUpdateInterval() {
        return updateInterval;
    }

    public void setUpdateInterval(int updateInterval) {
        this.updateInterval = updateInterval;
    }

    public LocalDateTime getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(LocalDateTime lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    @Override
    public String toString() {
        return name;
    }

    /*
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Environment that = (Environment) o;

        if (updateInterval != that.updateInterval) return false;
        //new array because persistentbag is compared by identity
        //i don't need this anymore...TODO check
        if (matchConfigs != null ? !new ArrayList<MatchConfig>(matchConfigs).equals(new ArrayList<MatchConfig>(that.matchConfigs)) : that.matchConfigs != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (path != null ? !path.equals(that.path) : that.path != null) return false;
        return !(server != null ? !server.equals(that.server) : that.server != null);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (server != null ? server.hashCode() : 0);
        result = 31 * result + (path != null ? path.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + updateInterval;
        result = 31 * result + (matchConfigs != null ? matchConfigs.hashCode() : 0);
        return result;
    }*/

    /*
    @XmlTransient
    @Transient
    public String getNextUpdate() {
        if (getLastUpdate() == null) {
            return SOON;
        }
        return HOURS_MINUTES.print(getLastUpdate().plusMinutes(getUpdateInterval()));
    }*/
}
