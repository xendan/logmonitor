package org.xendan.logmonitor.read;

import java.util.List;

import org.joda.time.DateTime;
import org.xendan.logmonitor.LogErrorsService;
import org.xendan.logmonitor.model.HostSettings;
import org.xendan.logmonitor.model.LogEntry;

public class ReadLogsTask implements Runnable {

    private final LogErrorsService service;
    private final LogReader reader;
    private final DateTime dateTime;

    public ReadLogsTask(LogErrorsService service, LogReader reader, DateTime dateTime) {
        this.service = service;
        this.reader = reader;
        this.dateTime = dateTime;
    }

    public void run() {
        List<LogEntry> entries = reader.readSince(dateTime);
        service.updateErrorsData(entries);
    }

}
