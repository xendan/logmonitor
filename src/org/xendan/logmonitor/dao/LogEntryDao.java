package org.xendan.logmonitor.dao;

import org.xendan.logmonitor.model.LogEntry;
import org.xendan.logmonitor.model.ServerSettings;

import java.util.List;

/**
 * User: id967161
 * Date: 09/09/13
 */
public interface LogEntryDao {
    LogEntry getLastEntry(ServerSettings settings);

    void addEntries(List<LogEntry> entries, ServerSettings settings);
}
