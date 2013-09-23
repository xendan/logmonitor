package org.xendan.logmonitor.service;

import org.xendan.logmonitor.model.Configuration;
import org.xendan.logmonitor.model.Environment;
import org.xendan.logmonitor.model.LogEntry;
import org.xendan.logmonitor.model.MatchConfig;

import java.util.List;

/**
 * User: id967161
 * Date: 20/09/13
 */
public interface LogService {
    List<Configuration> getConfigs();

    List<LogEntry> getMatchedEntries(MatchConfig matchConfig);

    void addMatchConfig(MatchConfig matcher, MatchConfig parentMatcher, Environment settings);

    void clearEntries(Environment settings);

    LogEntry getLastEntry(Environment environment);

    void addEntries(List<LogEntry> entries);

    void saveConfigs(List<Configuration> configs);
}
