package org.xendan.logmonitor;

import org.junit.Test;
import org.xendan.logmonitor.model.LogEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * User: kcyxa
 * Date: 1/26/13
 */
public class LogErrorsServiceTest {

    @Test
    public void testUpdateErrorsData() throws Exception {
        LogErrorsService service = new LogErrorsService();
        List<LogEntry> entries = createTestEntries();
        service.updateErrorsData(entries);
    }

    private List<LogEntry> createTestEntries() {
        List<LogEntry> entries = new ArrayList<LogEntry>();
        entries.add(createEntry());
        return entries;
    }

    private LogEntry createEntry() {
        LogEntry entry = new LogEntry();
        return entry;
    }
}
