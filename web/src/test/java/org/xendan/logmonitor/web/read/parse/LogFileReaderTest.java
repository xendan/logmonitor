package org.xendan.logmonitor.web.read.parse;


import org.apache.commons.io.IOUtils;
import org.apache.log4j.Level;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.xendan.logmonitor.HomeResolver;
import org.xendan.logmonitor.model.Environment;
import org.xendan.logmonitor.model.LogEntry;
import org.xendan.logmonitor.model.MatchConfig;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class LogFileReaderTest {

    public static final String DEF_PATTERN = "%d %-5p [%c] %m%n";

    @Test @Ignore("TODO: implement")
    public void testLogbackWithCustomPattern() throws Exception {
        String logPath = copyToTestPath("custom.log");
        Environment environment = new Environment();
        LogFileReader reader = new LogFileReader(logPath, "%-32([%d{yyyy-MM-dd HH:mm:ss:SSS}] [${storeNumber}]) %-30([%thread]) %-7([%level]) %-40([%logger{0}]): %msg%n", environment);
        assertNotNull("Expect file match pattern", reader.getEntries());
    }

    @Test
    public void testReadFromGoodFile() throws Exception {
        String logPath = copyToTestPath("no_match_spring.log");
        Environment environment = new Environment();
        LogFileReader reader = new LogFileReader(logPath, DEF_PATTERN, environment);
        assertNotNull("Expect file match pattern", reader.getEntries());
        assertEquals("Expect last update date",
                LogParserTest.defaultFrmtDate("2013-11-12 13:30:52,352"), environment.getLastUpdate());
    }

    @Test
    public void testReadFromBadFile() throws Exception {
        String logPath = copyToTestPath("bad_form.log");
        LogFileReader reader = new LogFileReader(logPath, LogParserTest.FULL_PATTERN, new Environment());
        assertNull(reader.getEntries());
    }

    @Test
    public void test_read() throws Exception {
        String logPath = copyToTestPath("bigexample.log");
        LogFileReader logParser = new LogFileReader(logPath, DEF_PATTERN,  createMatchers(Level.WARN));
        assertEquals("Expect last 13837 warnings loaded", 13837, logParser.getEntries().size());
    }

    private String copyToTestPath(String filePath) throws IOException {
        HomeResolver resolver = new HomeResolver();
        return copyFromResource(resolver, "test", filePath);
    }

    @Test
    public void test_join() throws Exception {
        String logPath = copyToTestPath("no_match_spring.log");
        LogFileReader logParser = new LogFileReader(logPath, DEF_PATTERN,  createMatchers(Level.ERROR));
        List<LogEntry> entries = logParser.getEntries();
        Assert.assertEquals(4, entries.size());
        String message = entries.get(0).getMessage();
        assertTrue("Real end is " + message.substring(message.length() - 10),
                message.endsWith("... 3 more"));

    }

    private Environment createMatchers(Level level) {
        Environment env = new Environment();
        MatchConfig matcher = new MatchConfig();
        matcher.setLevel(level.toString());
        env.getMatchConfigs().add(matcher);
        env.setLastUpdate(LogParserTest.A_WHILE_AGO);
        return env;
    }

    private String copyFromResource(HomeResolver resolver, String dir, String filePath) throws IOException {
        File file = new File(resolver.joinMkDirs(filePath, dir));
        if (!file.exists()) {
            if (!file.createNewFile()) {
                throw new IllegalStateException("Error creating file" + file.getAbsolutePath());
            }
        }
        IOUtils.copy(getClass().getResourceAsStream("/org/xendan/logmonitor/web/read/parse/" + filePath), new FileOutputStream(file, false));
        return file.getAbsolutePath();
    }
}
