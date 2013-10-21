package org.xendan.logmonitor.dao;

import org.xendan.logmonitor.model.*;

import java.util.List;

/**
 * User: id967161
 * Date: 04/09/13
 */
public interface ConfigurationDao {

    void save(List<Configuration> configs);

    List<Configuration> getConfigs();

    LogEntry getLastEntry(Environment settings);

    void addEntries(List<LogEntry> entries);

    List<LogEntry> getNotGroupedMatchedEntries(MatchConfig matchConfig, Environment environment);

    List<LogEntryGroup> getMatchedEntryGroups(MatchConfig matchConfig, Environment environment);

    void addMatchConfig(MatchConfig matcher, MatchConfig parentMatcher, Environment settings);

    void clearEntries(Environment settings);
}
