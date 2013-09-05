package org.xendan.logmonitor;

import org.apache.log4j.Level;
import org.junit.Test;
import org.xendan.logmonitor.dao.LogErrorDao;
import org.xendan.logmonitor.model.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * User: kcyxa
 * Date: 1/26/13
 */
public class LogErrorsServiceTest {

    @Test
    public void testUpdateErrors() throws Exception {
        MyLogErrorMockDao dao = new MyLogErrorMockDao();
        LogErrorsService service = new LogErrorsService(dao);

        service.updateErrors(createOldErrorData(), createTestEntries());

        LogErrorData updated = dao.getUpdated();
        assertEquals("Expect one error added",
                1, updated.getFoundErrors().size());
    }

    private LogErrorData createOldErrorData() {
        LogErrorData data = new LogErrorData();
        data.setEntryMatchers(createMatchers());
        data.setFoundErrors(new ArrayList<FoundError>());
        return data;
    }

    private List<EntryMatcher> createMatchers() {
        List<EntryMatcher> matchers = new ArrayList<EntryMatcher>();
        matchers.add(createSimpleMatcher());
        return matchers;
    }

    private EntryMatcher createSimpleMatcher() {
        EntryMatcher matcher = new EntryMatcher();
        matcher.setLevel(Level.ERROR.toString());
        return matcher;
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

    private static class MyLogErrorMockDao implements LogErrorDao {

        private LogErrorData updated;

        @Override
        public void updateErrorData(LogErrorData logErrorData) {
            updated = logErrorData;
        }

        @Override
        public LogErrorData getErrorData(ServerSettings settings) {
            return null;
        }

        public LogErrorData getUpdated() {
            return updated;
        }
    }
}
