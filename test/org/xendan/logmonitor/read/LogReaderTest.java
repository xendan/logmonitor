package org.xendan.logmonitor.read;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.junit.Before;
import org.junit.Test;
import org.xendan.logmonitor.model.LogEntry;
import org.xendan.logmonitor.parser.LogParserTest;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LogReaderTest {

    private LogDownloader iAmFake;

    @Before
    public void setUp() {
        iAmFake = mock(LogDownloader.class);
    }

    @Test
    public void test_read() throws Exception {
        when(iAmFake.downloadToLocal()).thenReturn("test/reader_test.log");

        LogReader reader = new LogReader(LogParserTest.PATTERN, iAmFake);
        DateTime lastDate = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss,SSS").parseDateTime("2012-09-28 00:00:02,044");
        List<LogEntry> entries = reader.readSince(lastDate);
        assertEquals("Expect all entries before lastDate are skipped", 6, entries.size());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void test_read_invalid_format() throws Exception {
        when(iAmFake.downloadToLocal()).thenReturn("test/reader_test.log");

        LogReader reader = new LogReader("%d{yyyy-MM-dd HH:mm:ss,SSS} THIS IS INVALID FORMAT %-5p", iAmFake);
        reader.readSince(new DateTime());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void test_read_invalid_file() throws Exception {
        when(iAmFake.downloadToLocal()).thenReturn("does_not_exist.log");

        LogReader reader = new LogReader(LogParserTest.PATTERN, iAmFake);
        reader.readSince(new DateTime());
    }
}
