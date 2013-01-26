package org.xendan.logmonitor.read;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.junit.Test;
import org.xendan.logmonitor.dao.LogEntryDao;
import org.xendan.logmonitor.model.LogEntry;
import org.xendan.logmonitor.read.LogReader;
import org.xendan.logmonitor.read.ReadLogsTask;

public class ReadLogsTaskTest {

    @Test
    public void testRun() throws Exception {
        LogEntryDao dao = mock(LogEntryDao.class);
        LogReader reader = mock(LogReader.class);
        ReadLogsTask task = new ReadLogsTask(dao, reader);
        DateTime lastDate = new DateTime();
        
        when(dao.getLastDate()).thenReturn(lastDate);
        List<LogEntry> entries = new ArrayList<LogEntry>();
        when(reader.readSince(lastDate)).thenReturn(entries);
        
        task.run();

        verify(reader).readSince(lastDate);
        verify(dao).addEntries(entries);
    }

}
