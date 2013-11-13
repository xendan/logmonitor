package org.xendan.logmonitor.read;

import junit.framework.Assert;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Level;
import org.junit.Test;
import org.xendan.logmonitor.HomeResolver;
import org.xendan.logmonitor.model.Environment;
import org.xendan.logmonitor.model.LogEntry;
import org.xendan.logmonitor.model.MatchConfig;
import org.xendan.logmonitor.parser.LogFileReader;
import org.xendan.logmonitor.parser.LogParserTest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.assertEquals;

/**
 * User: id967161
 * Date: 09/09/13
 */
public class LogFileParserTest {

    public static final String DEF_PATTERN = "%d %-5p [%c] %m%n";

    @Test
    public void test_read() throws Exception {
        String logPath = copyToTestPath("bigexample.log");
        LogFileReader logParser = new LogFileReader(LogParserTest.A_WHILE_AGO, logPath, DEF_PATTERN,  createMatchers(Level.WARN));
        assertEquals("Expect last 13837 warnings loaded", 13837, logParser.getEntries().size());
    }

    private String copyToTestPath(String filePath) throws IOException {
        HomeResolver resolver = new HomeResolver();
        return copyFromResource(resolver, "test", filePath);
    }

    @Test
    public void test_join() throws Exception {
        String logPath = copyToTestPath("no_match_spring.log");
        LogFileReader logParser = new LogFileReader(LogParserTest.A_WHILE_AGO, logPath, DEF_PATTERN,  createMatchers(Level.ERROR));
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
        return env;
    }

    private String copyFromResource(HomeResolver resolver, String dir, String filePath) throws IOException {
        File file = new File(resolver.joinMkDirs(filePath, dir));
        if (!file.exists()) {
            if (!file.createNewFile()) {
                throw new IllegalStateException("Error creating file" + file.getAbsolutePath());
            }
        }
        IOUtils.copy(getClass().getResourceAsStream("/org/xendan/logmonitor/read/" + filePath), new FileOutputStream(file, false));
        return file.getAbsolutePath();
    }
}
