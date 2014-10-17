package org.xendan.logmonitor.model;

/**
 * @author mullomuk
 * @since 5/8/2014.
 */
public class EnvironmentStatus {
    private final String message;
    private final boolean isError;
    private final String stackTrace;
    private long updateInterval;

    public EnvironmentStatus(String message, boolean isError) {
        this.message = message;
        this.isError = isError;
        stackTrace = null;
    }

    public EnvironmentStatus(String message, String stackTrace) {
        this.stackTrace = stackTrace;
        this.message = message;
        isError = true;
    }

    public String getMessage() {
        return message;
    }

    public boolean isError() {
        return isError;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public long getUpdateInterval() {
        return updateInterval;
    }

    public void setUpdateInterval(long updateInterval) {
        this.updateInterval = updateInterval;
    }
}
