package org.xendan.logmonitor.model;

import java.io.Serializable;

/**
 * User: kcyxa
 * Date: 1/26/13
 */
public class MatchPattern implements Serializable {
    private String level;
    private boolean error;
    private String name;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
