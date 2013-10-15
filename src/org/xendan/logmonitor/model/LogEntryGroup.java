package org.xendan.logmonitor.model;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import java.util.ArrayList;
import java.util.List;

/**
 * User: id967161
 * Date: 15/10/13
 */
@Entity
public class LogEntryGroup extends BaseObject {
    private String messagePattern;
    private List<LogEntry> entries = new ArrayList<LogEntry>();

    @Column(columnDefinition="text")
    public String getMessagePattern() {
        return messagePattern;
    }

    public void setMessagePattern(String messagePattern) {
        this.messagePattern = messagePattern;
    }

    @ManyToMany(cascade = CascadeType.ALL)
    public List<LogEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<LogEntry> entries) {
        this.entries = entries;
    }
}
