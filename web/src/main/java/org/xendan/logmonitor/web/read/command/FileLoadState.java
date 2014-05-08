package org.xendan.logmonitor.web.read.command;

import org.apache.tools.ant.BuildException;

/**
 * User: id967161
 * Date: 20/03/14
 */
public class FileLoadState {
    volatile private String localPath;
    volatile long fileSize;
    volatile private String errorMessage;
    volatile private Throwable exception;
    volatile private boolean entriesNotFound;
    volatile private State state = State.WAITING_UPDATE;

    public void setDownloadPrepareStart(String localPath) {
        this.localPath = localPath;
        state = State.WAITING_DOWNLOAD_START;
        exception = null;
        errorMessage = null;
        entriesNotFound = false;
    }

    public void setFileSizeCalculated(String fileSize) {
        try {
            this.fileSize = Long.valueOf(fileSize);
        } catch (NumberFormatException e) {
            this.fileSize = -1;
        }

        state = State.WAITING_DOWNLOAD;
    }

    public void onSshError(String errorMessage, BuildException exception) {
        this.errorMessage = errorMessage;
        this.exception = exception;
        state = State.WAITING_UPDATE;
    }

    public void setEntriesNotFound() {
        this.entriesNotFound = true;
        state = State.WAITING_UPDATE;
    }

    public void onParseEntriesStarted() {
        state = State.WAITING_PARSE;
    }

    public void onEntriesAdded() {
        state = State.WAITING_UPDATE;
    }

    public void onAddEntriesError(Throwable error) {
        this.exception = error;
        this.errorMessage = "Error saving entries to DB";
        state = State.WAITING_UPDATE;
    }

    public String getLocalPath() {
        return localPath;
    }

    public long getFileSize() {
        return fileSize;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Throwable getException() {
        return exception;
    }

    public State getState() {
        return state;
    }

    public boolean isEntriesNotFound() {
        return entriesNotFound;
    }

    public enum State {
        WAITING_DOWNLOAD_START, WAITING_DOWNLOAD, WAITING_PARSE, WAITING_UPDATE

    }
}
