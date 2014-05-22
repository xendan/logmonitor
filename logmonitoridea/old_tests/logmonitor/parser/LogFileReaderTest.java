package org.xendan.logmonitor.parser;

import org.junit.Test;
import org.xendan.logmonitor.model.Environment;
import static org.junit.Assert.*;

/**
 * User: id967161
 * Date: 19/03/14
 */
public class LogFileReaderTest {

    @Test
    public void testReadFromGoodFile() throws Exception {
        Environment environment = new Environment();
        LogFileReader reader = new LogFileReader("test/org/xendan/logmonitor/read/no_match_spring.log", LogParserTest.FULL_PATTERN, environment);
        assertNotNull(reader.getEntries());
        assertEquals("Expect last update date",
                LogParserTest.defaultFrmtDate("2013-11-12 13:30:52,352"), environment.getLastUpdate());
    }

    @Test
    public void testReadFromBadFile() throws Exception {
        LogFileReader reader = new LogFileReader("test/org/xendan/logmonitor/parser/bad_form.log", LogParserTest.FULL_PATTERN, new Environment());
        assertNull(reader.getEntries());
    }
}
