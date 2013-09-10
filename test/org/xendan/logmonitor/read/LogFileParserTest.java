package org.xendan.logmonitor.read;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.xendan.logmonitor.HomeResolver;
import org.xendan.logmonitor.parser.LogFileParser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

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
        String project = "thisistes";
        String logPath = copyFromResource(resolver, "test", TEST_LOG);
        LogFileParser logParser = new LogFileParser(logPath, "%d %-5p [%c] %m%n",  new MatcherService(resolver).getLocalMatchers(project, null).getMatchers());
        assertEquals("Expect last 13837 warnings loaded", 13837, logParser.getEntries().size());
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
