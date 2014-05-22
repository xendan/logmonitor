package org.xendan.logmonitor.parser;

import org.xendan.logmonitor.model.Environment;
import org.xendan.logmonitor.model.LogEntry;
import org.xendan.logmonitor.model.MatchConfig;

import java.util.Collections;

/**
 * User: id967161
 * Date: 09/09/13
 */
public class EntryMatcher {

    private final Environment environment;

    public EntryMatcher(Environment environment) {
        this.environment = environment;
        Collections.sort(environment.getMatchConfigs());
    }

    public boolean match(LogEntry entry) {
        for (MatchConfig matchPattern : environment.getMatchConfigs()) {
            if (doMatch(matchPattern, entry)) {
                return true;
            }
        }
        return false;
    }

    private boolean doMatch(MatchConfig matchPattern, LogEntry entry) {
        boolean match = PatternUtils.isMatch(entry, matchPattern);
        if (match) {
            entry.setEnvironment(environment);
            entry.setMatchConfig(matchPattern);
        }
        return match;
    }
}
