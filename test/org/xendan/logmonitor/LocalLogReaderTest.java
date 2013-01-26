package org.xendan.logmonitor;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.junit.Test;
import org.xendan.logmonitor.model.LogEntry;
import org.xendan.logmonitor.parser.LogParserTest;

public class LocalLogReaderTest {

    @Test
    public void test_read() throws Exception {
        LocalLogReader reader = new LocalLogReader(LogParserTest.PATTERN, "test/reader_test.log");
        DateTime lastDate = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss,SSS").parseDateTime("2012-09-28 00:00:02,044");
        List<LogEntry> entries = reader.readSince(lastDate);
        assertEquals("Expect all entries before lastDate are skipped", 6, entries.size());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void test_read_invalid_format() throws Exception {
        LocalLogReader reader = new LocalLogReader("%d{yyyy-MM-dd HH:mm:ss,SSS} THIS IS INVALID FORMAT %-5p", "src/test/resources/reader_test.log");
        reader.readSince(new DateTime());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void test_read_invalid_file() throws Exception {
        LocalLogReader reader = new LocalLogReader(LogParserTest.PATTERN, "src/test/resources/does_not_exist.log");
        reader.readSince(new DateTime());
    }
    
}
