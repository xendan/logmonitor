package org.xendan.logmonitor.model;

import java.util.List;

public class EntriesList {

    private final List<LogEntryGroup> groups;

    private final List<LogEntry> notGrouped;

    public EntriesList(List<LogEntryGroup> groups, List<LogEntry> notGrouped) {
        this.groups = groups;
        this.notGrouped = notGrouped;
    }

    public List<LogEntryGroup> getGroups() {
        return groups;
    }

    public List<LogEntry> getNotGrouped() {
        return notGrouped;
    }
}
