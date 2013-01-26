package org.xendan.logmonitor;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.junit.Test;
import org.xendan.logmonitor.dao.LogEntryDao;
import org.xendan.logmonitor.model.LogEntry;
public class ReadLogsTaskTest {

    @Test
    public void testRun() throws Exception {
        LogEntryDao dao = mock(LogEntryDao.class);
        LocalLogReader reader = mock(LocalLogReader.class);
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
