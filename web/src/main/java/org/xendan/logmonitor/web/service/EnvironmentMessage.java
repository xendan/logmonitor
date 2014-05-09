package org.xendan.logmonitor.web.service;

/**
* Created by ksenia on 09.05.14.
*/
public enum EnvironmentMessage {
    WAITING("Wait next update"),
    DOWNLOADING("Downloading..."),
    PARSING("Parsing"),

    //ERRORS
    ERROR_DOWNLOADING("Error downloading"),
    FILE_NOT_FOUND("File not found"),
    NO_ENTRIES_FOUND("No entries found"),
    ERROR_GETTING_FILE_SIZE("Error calculating total file size");
    private String text;

    EnvironmentMessage(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
