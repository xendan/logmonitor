package org.xendan.logmonitor.parser;

import org.apache.log4j.Level;
import org.xendan.logmonitor.model.LogEntry;
import org.xendan.logmonitor.model.MatchConfig;

import javax.persistence.Entity;
import java.util.List;

/**
 * User: id967161
 * Date: 09/09/13
 */
@Entity
public class EntryMatcher {

    private final List<MatchConfig> matchers;

    public EntryMatcher(List<MatchConfig> matchers) {
        this.matchers = matchers;
    }


    public boolean match(LogEntry entry) {
        for (MatchConfig matchPattern : matchers) {
            if (match(matchPattern, entry)) {
                return true;
            }
        }
        return false;
    }

    private boolean match(MatchConfig matchPattern, LogEntry entry) {
        Level matchLEvel = Level.toLevel(matchPattern.getLevel());
        Level entryLevel = Level.toLevel(entry.getLevel());
        boolean match = entryLevel.isGreaterOrEqual(matchLEvel);
        if (match) {
            entry.setMatcher(matchPattern);
        }
        return match;
    }
}
