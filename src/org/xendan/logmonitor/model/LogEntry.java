package org.xendan.logmonitor.model;

import org.joda.time.DateTime;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.io.Serializable;

public class LogEntry implements Serializable {
    private Long id;
    private ServerSettings severSettings;
    private DateTime date;
    private String caller;
    private String message;
    private String category;
    private Integer lineNumber;
    private String level;

    @Id
    @GeneratedValue
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public DateTime getDate() {
        return date;
    }

    public void setDate(DateTime date) {
        this.date = date;
    }

    public String getCaller() {
        return caller;
    }

    public void setCaller(String caller) {
        this.caller = caller;
    }

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

    public ServerSettings getSeverSettings() {
        return severSettings;
    }

    public void setSeverSettings(ServerSettings severSettings) {
        this.severSettings = severSettings;
    }
}
