package org.xendan.logmonitor;

import java.util.List;

import org.xendan.logmonitor.dao.LogEntryDao;
import org.xendan.logmonitor.model.LogEntry;

public class ReadLogsTask implements Runnable {

    private final LogEntryDao dao;
    private final LocalLogReader reader;

    public ReadLogsTask(LogEntryDao dao, LocalLogReader reader) {
        this.dao = dao;
        this.reader = reader;
    }

    public void run() {
        List<LogEntry> newEntries = reader.readSince(dao.getLastDate());
        dao.addEntries(newEntries);
    }

}
