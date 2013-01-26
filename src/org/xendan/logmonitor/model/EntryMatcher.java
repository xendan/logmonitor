package org.xendan.logmonitor.model;

/**
 * User: kcyxa
 * Date: 1/26/13
 */
public class EntryMatcher {
    private String level;
    private boolean error;

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }
}
