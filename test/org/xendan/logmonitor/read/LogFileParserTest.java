package org.xendan.logmonitor.read;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Level;
import org.junit.Test;
import org.xendan.logmonitor.HomeResolver;
import org.xendan.logmonitor.model.MatchConfig;
import org.xendan.logmonitor.parser.LogFileParser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static junit.framework.TestCase.assertEquals;

/**
 * User: id967161
 * Date: 09/09/13
 */
public class LogFileParserTest {

    private static final String TEST_LOG = "bigexample.log";

    @Test
    public void test_read() throws Exception {
        HomeResolver resolver = new HomeResolver();
        String logPath = copyFromResource(resolver, "test", TEST_LOG);
        LogFileParser logParser = new LogFileParser(logPath, "%d %-5p [%c] %m%n",  createMatchers());
        assertEquals("Expect last 13837 warnings loaded", 13837, logParser.getEntries().size());
    }

    private List<MatchConfig> createMatchers() {
        MatchConfig matcher = new MatchConfig();
        matcher.setLevel(Level.WARN.toString());
        return Arrays.asList(matcher);
    }

    private String copyFromResource(HomeResolver resolver, String dir, String filePath) throws IOException {
        File file = new File(resolver.joinMkDirs(filePath, dir));
        if (!file.exists()) {
            if (!file.createNewFile()) {
                throw new IllegalStateException("Error creating file" + file.getAbsolutePath());
            }
        }
        IOUtils.copy(getClass().getResourceAsStream("/" + filePath), new FileOutputStream(file, false));
        return file.getAbsolutePath();
    }
}
