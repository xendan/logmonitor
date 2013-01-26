package org.xendan.logmonitor.read;

import org.xendan.logmonitor.LogErrorsService;
import org.xendan.logmonitor.model.HostSettings;
import org.xendan.logmonitor.model.LogEntry;
import org.xendan.logmonitor.model.LogErrorData;

import java.util.List;

public class ReadLogsTask implements Runnable {

    private final LogErrorsService service;
    private final LogReader reader;
    private final HostSettings settings;

    public ReadLogsTask(LogErrorsService service, LogReader reader, HostSettings settings) {
        this.service = service;
        this.reader = reader;
        this.settings = settings;
    }

    public void run() {
        LogErrorData data = service.getLogErrorData(settings);
        List<LogEntry> newEntries = reader.readSince(data.getUpdateDateTime());
        service.updateErrors(data, newEntries);
    }

}
