package org.xendan.logmonitor.parser;

import org.apache.log4j.Level;
import org.xendan.logmonitor.model.LogEntry;
import org.xendan.logmonitor.model.MatchPattern;
import org.xendan.logmonitor.model.Matchers;

/**
 * User: id967161
 * Date: 09/09/13
 */
public class EntryMatcher {

    private final Matchers matchers;

    public EntryMatcher(Matchers matchers) {
        this.matchers = matchers;
    }

    public boolean match(LogEntry entry) {
        for (MatchPattern matchPattern : matchers.getMatchers()) {
            if (match(matchPattern, entry)) {
                return true;
            }
        }
        return false;
    }

    private boolean match(MatchPattern matchPattern, LogEntry entry) {
        Level matchLEvel = Level.toLevel(matchPattern.getLevel());
        Level entryLevel = Level.toLevel(entry.getLevel());
        return entryLevel.isGreaterOrEqual(matchLEvel);
    }
}
