package org.xendan.logmonitor.model;

import org.hibernate.annotations.Type;
import org.joda.time.LocalDateTime;

import javax.persistence.*;

@Entity
public class LogEntry extends BaseObject {
    private Long id;
    private LocalDateTime date;
    private String caller;
    private String message;
    private String category;
    private Integer lineNumber;
    private String level;
    private MatchConfig matchConfig;
    private LogSettings logSettings;
    private int foundNumber;

    @Id
    @GeneratedValue
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(length = 1000)
    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Integer getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(Integer lineNumber) {
        this.lineNumber = lineNumber;
    }

    @Type(type="org.jadira.usertype.dateandtime.joda.PersistentLocalDateTime")
    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public String getCaller() {
        return caller;
    }

    public void setCaller(String caller) {
        this.caller = caller;
    }

    @Column(columnDefinition="text")
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    @ManyToOne(cascade = {CascadeType.ALL})
    public MatchConfig getMatchConfig() {
        return matchConfig;
    }

    public void setMatchConfig(MatchConfig matchConfig) {
        this.matchConfig = matchConfig;
    }

    @ManyToOne(cascade = {CascadeType.ALL})
    public LogSettings getLogSettings() {
        return logSettings;
    }

    public void setLogSettings(LogSettings logSettings) {
        this.logSettings = logSettings;
    }

    public int getFoundNumber() {
        return foundNumber;
    }

    public void setFoundNumber(int foundNumber) {
        this.foundNumber = foundNumber;
    }

    public LogEntry createCopy(int foundNumber, MatchConfig matcher) {
        LogEntry copy = new LogEntry();
        copy.setMatchConfig(matcher);
        copy.setMessage(message);
        copy.setLevel(level);
        copy.setDate(date);
        copy.setCaller(caller);
        copy.setCategory(category);
        copy.setLineNumber(lineNumber);
        copy.setFoundNumber(foundNumber);
        return copy;
    }
}
