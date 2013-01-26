package org.xendan.logmonitor;

import org.apache.log4j.Level;
import org.xendan.logmonitor.model.*;

import java.util.List;

/**
 * User: kcyxa
 * Date: 1/26/13
 */
public class LogErrorsService {

    private LogErrorDao dao;

    public LogErrorsService(LogErrorDao dao) {
        this.dao = dao;
    }

    public void updateErrors(LogErrorData data, List<LogEntry> entries) {
        for (EntryMatcher matcher : data.getEntryMatchers()) {
            for (LogEntry entry : entries) {
                if (matches(entry, matcher)) {
                    data.getFoundErrors().add(createError(entry, matcher));
                }
            }
        }
        dao.updateErrorData(data);
    }

    private FoundError createError(LogEntry entry, EntryMatcher matcher) {
        FoundError error = new FoundError();
        error.setEntry(entry);
        error.setMatcher(matcher);
        return error;
    }

    private boolean matches(LogEntry entry, EntryMatcher matcher) {
        if (matcher.getLevel() != null) {
            Level entryLevel = Level.toLevel(entry.getLevel());
            Level matcherLevel = Level.toLevel(matcher.getLevel());
            if (entryLevel.isGreaterOrEqual(matcherLevel)) {
                return true;
            }
        }
        return false;
    }

    public LogErrorData getLogErrorData(HostSettings settings) {
        return dao.getErrorData(settings);
    }
}
