package org.xendan.logmonitor.read;

import java.util.List;

import org.xendan.logmonitor.dao.LogEntryDao;
import org.xendan.logmonitor.model.LogEntry;

public class ReadLogsTask implements Runnable {

    private final LogEntryDao dao;
    private final LogReader reader;

    public ReadLogsTask(LogEntryDao dao, LogReader reader) {
        this.dao = dao;
        this.reader = reader;
    }

    public void run() {
        List<LogEntry> newEntries = reader.readSince(dao.getLastDate());
        dao.addEntries(newEntries);
    }

}
