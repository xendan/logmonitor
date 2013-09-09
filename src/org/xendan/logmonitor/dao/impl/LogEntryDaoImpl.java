package org.xendan.logmonitor.dao.impl;

import org.xendan.logmonitor.dao.LogEntryDao;
import org.xendan.logmonitor.model.LogEntry;
import org.xendan.logmonitor.model.ServerSettings;

import java.util.List;

/**
 * User: id967161
 * Date: 09/09/13
 */
public class LogEntryDaoImpl implements LogEntryDao {
    @Override
    public LogEntry getLastEntry(ServerSettings settings) {
        return null;
    }

    @Override
    public void addEntries(List<LogEntry> entries, ServerSettings settings) {
    }
}
