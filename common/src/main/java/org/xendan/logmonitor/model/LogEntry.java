package org.xendan.logmonitor.model;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.annotations.Type;
import org.joda.time.LocalDateTime;

import javax.persistence.*;

@Entity
public class LogEntry extends BaseObject {
    private LocalDateTime date;
    private String message;
    private MatchConfig matchConfig;
    private Environment environment;
    private String expandedMessage;
    private int foundNumber;
    private String level;
    private Map<String, String> properties = new HashMap<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyColumn(name="name")
    @Column(name="value")
    @CollectionTable(name="log_entry_properties", joinColumns=@JoinColumn(name="property_id"))
    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    @Column(columnDefinition="text")
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @ManyToOne
    public MatchConfig getMatchConfig() {
        return matchConfig;
    }

    public void setMatchConfig(MatchConfig matchConfig) {
        this.matchConfig = matchConfig;
    }

    @ManyToOne
    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Transient
    public String getExpandedMessage() {
        return expandedMessage;
    }

    public void setExpandedMessage(String expandedMessage) {
        this.expandedMessage = expandedMessage;
    }


    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public int getFoundNumber() {
        return foundNumber;
    }

    public void setFoundNumber(int foundNumber) {
        this.foundNumber = foundNumber;
    }
}
