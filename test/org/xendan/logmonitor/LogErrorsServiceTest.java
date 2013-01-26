package org.xendan.logmonitor;

import org.apache.log4j.Level;
import org.junit.Test;
import org.xendan.logmonitor.model.EntryMatcher;
import org.xendan.logmonitor.model.LogEntry;
import org.xendan.logmonitor.model.LogErrorData;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.refEq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * User: kcyxa
 * Date: 1/26/13
 */
public class LogErrorsServiceTest {

    @Test
    public void testUpdateErrors() throws Exception {
        LogErrorDao dao = mock(LogErrorDao.class);
        LogErrorsService service = new LogErrorsService(dao);

        service.updateErrors(createOldErrorData(), createTestEntries());

        verify(dao).updateErrorData(refEq(createExpectedUpdatedData()));
    }

    private LogErrorData createOldErrorData() {
        LogErrorData data = new LogErrorData();
        data.setEntryMatchers(createMatchers());
        return data;
    }

    private List<EntryMatcher> createMatchers() {
        List<EntryMatcher> matchers = new ArrayList<EntryMatcher>();
        matchers.add(createSimpleMatcher());
        return matchers;
    }

    private EntryMatcher createSimpleMatcher() {
        EntryMatcher matcher = new EntryMatcher();
        return matcher;
    }

    private LogErrorData createExpectedUpdatedData() {
        LogErrorData data = new LogErrorData();
        return data;
    }

    private List<LogEntry> createTestEntries() {
        List<LogEntry> entries = new ArrayList<LogEntry>();
        entries.add(createEntry());
        return entries;
    }

    private LogEntry createEntry() {
        LogEntry entry = new LogEntry();
        entry.setLevel(Level.ERROR.toString());
        return entry;
    }
}
