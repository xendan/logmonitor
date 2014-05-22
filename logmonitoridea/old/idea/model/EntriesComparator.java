package org.xendan.logmonitor.idea.model;

import org.xendan.logmonitor.model.LogEntry;

import java.util.Comparator;
import java.util.List;

/**
* User: id967161
* Date: 27/11/13
*/
class EntriesComparator implements Comparator<LogEntry> {
    private final List<LogEntry> newEntries;

    public EntriesComparator(List<LogEntry> newEntries) {
        this.newEntries = newEntries;
    }

    @Override
    public int compare(LogEntry o1, LogEntry o2) {
        if (newEntries != null) {
            boolean new1 = newEntries.contains(o1);
            boolean new2 = newEntries.contains(o2);
            if (new1 && !new2) {
                return -1;
            }
            if (new2 && !new1) {
                return 1;
            }
        }
        return o2.getDate().compareTo(o1.getDate());
    }
}
