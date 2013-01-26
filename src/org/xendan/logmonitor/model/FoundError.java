package org.xendan.logmonitor.model;

/**
 * User: kcyxa
 * Date: 1/26/13
 */
public class FoundError {

    private LogEntry entry;
    private EntryMatcher matcher;

    public LogEntry getEntry() {
        return entry;
    }

    public void setEntry(LogEntry entry) {
        this.entry = entry;
    }

    public EntryMatcher getMatcher() {
        return matcher;
    }

    public void setMatcher(EntryMatcher matcher) {
        this.matcher = matcher;
    }
}
