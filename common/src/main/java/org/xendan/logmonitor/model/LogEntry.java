package org.xendan.logmonitor.model;

import org.hibernate.annotations.Type;
import org.joda.time.LocalDateTime;

import javax.persistence.*;

@Entity
public class LogEntry extends BaseObject {
    private LocalDateTime date;
    private String caller;
    private String message;
    private String category;
    private Integer lineNumber;
    private String level;
    private MatchConfig matchConfig;
    private Environment environment;
    private String thread;
    private int foundNumber;
    private String fileName;
    private String locationInformation;
    private String methodName;
    private Integer elapsedTime;
    private String ndc;
    private String mdc;

    @Column(length = 1000)
    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Integer getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(Integer lineNumber) {
        this.lineNumber = lineNumber;
    }

    @Type(type="org.jadira.usertype.dateandtime.joda.PersistentLocalDateTime")
    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public String getCaller() {
        return caller;
    }

    public void setCaller(String caller) {
        this.caller = caller;
    }

    @Column(columnDefinition="text")
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    @ManyToOne
    public MatchConfig getMatchConfig() {
        return matchConfig;
    }

    public void setMatchConfig(MatchConfig matchConfig) {
        this.matchConfig = matchConfig;
    }

    @ManyToOne
    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public int getFoundNumber() {
        return foundNumber;
    }

    public void setFoundNumber(int foundNumber) {
        this.foundNumber = foundNumber;
    }

    public String getThread() {
        return thread;
    }

    public void setThread(String thread) {
        this.thread = thread;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getLocationInformation() {
        return locationInformation;
    }

    public void setLocationInformation(String locationInformation) {
        this.locationInformation = locationInformation;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Integer getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(Integer elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    public String getNdc() {
        return ndc;
    }

    public void setNdc(String ndc) {
        this.ndc = ndc;
    }

    public String getMdc() {
        return mdc;
    }

    public void setMdc(String mdc) {
        this.mdc = mdc;
    }
}
