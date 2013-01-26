package org.xendan.logmonitor.model;

import org.joda.time.DateTime;

import java.util.List;

/**
 * User: kcyxa
 * Date: 1/26/13
 */
public class LogErrorData {

    private DateTime updateDateTime;
    private List<EntryMatcher> entryMatchers;
    private List<FoundError> foundErrors;

    public DateTime getUpdateDateTime() {
        return updateDateTime;
    }

    public void setUpdateDateTime(DateTime updateDateTime) {
        this.updateDateTime = updateDateTime;
    }

    public List<EntryMatcher> getEntryMatchers() {
        return entryMatchers;
    }

    public void setEntryMatchers(List<EntryMatcher> entryMatchers) {
        this.entryMatchers = entryMatchers;
    }

    public List<FoundError> getFoundErrors() {
        return foundErrors;
    }

    public void setFoundErrors(List<FoundError> foundErrors) {
        this.foundErrors = foundErrors;
    }
}
