package org.xendan.logmonitor.dao;

import org.xendan.logmonitor.model.LogEntry;
import org.xendan.logmonitor.model.LogSettings;
import org.xendan.logmonitor.model.MatchConfig;

import java.util.List;

/**
 * User: id967161
 * Date: 09/09/13
 */
public interface LogEntryDao {
    LogEntry getLastEntry(LogSettings settings);

    void addEntries(List<LogEntry> entries);

    List<LogEntry> getMatchedEntries(MatchConfig matchConfig);

    void addMatchConfig(MatchConfig matcher, MatchConfig parentMatcher, LogSettings settings);

    void clearEntries(LogSettings settings);
}
