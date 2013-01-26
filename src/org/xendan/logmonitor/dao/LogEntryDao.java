package org.xendan.logmonitor.dao;

import java.util.List;

import org.joda.time.DateTime;
import org.xendan.logmonitor.model.LogEntry;

public interface LogEntryDao {

    DateTime getLastDate();

    void addEntries(List<LogEntry> entries);

}
