package org.xendan.logmonitor.parser;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.xendan.logmonitor.model.Environment;
import org.xendan.logmonitor.model.LogEntry;
import org.xendan.logmonitor.model.MatchConfig;

import javax.persistence.Entity;
import java.util.Collections;
import java.util.regex.Pattern;

/**
 * User: id967161
 * Date: 09/09/13
 */
@Entity
public class EntryMatcher {

    private final Environment environment;

    public EntryMatcher(Environment environment) {
        this.environment = environment;
        Collections.sort(environment.getMatchConfigs());
    }

    public boolean match(LogEntry entry) {
        for (MatchConfig matchPattern : environment.getMatchConfigs()) {
            if (match(matchPattern, entry)) {
                return true;
            }
        }
        return false;
    }

    private boolean match(MatchConfig matchPattern, LogEntry entry) {
        Level matchLevel = matchPattern.getLevel() == null ? null : Level.toLevel(matchPattern.getLevel());
        Level entryLevel = Level.toLevel(entry.getLevel());
        boolean match = (matchLevel == null) || entryLevel.isGreaterOrEqual(matchLevel);
        match = StringUtils.isEmpty(matchPattern.getMessage()) ? match : messageMatch(entry.getMessage(), matchPattern.getMessage());
        if (match) {
            entry.setEnvironment(environment);
            entry.setMatchConfig(matchPattern);
        }
        return match;
    }

    private boolean messageMatch(String message, String messagePattern) {
        return Pattern.compile(messagePattern, Pattern.CASE_INSENSITIVE).matcher(message).find();
    }
}
