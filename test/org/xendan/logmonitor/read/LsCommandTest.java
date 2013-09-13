package org.xendan.logmonitor.read;

import org.junit.Test;
import org.xendan.logmonitor.model.Server;

import static junit.framework.TestCase.assertEquals;

/**
 * User: id967161
 * Date: 13/09/13
 */
public class LsCommandTest {

    private final static String LS_OUTPUT = "/opt/java\n" +
            "/opt/java/jbossx\n" +
            "/opt/java/temp\n" +
            "/opt/java/appSettings\n" +
            "/opt/java/hyperic\n" +
            "/opt/java/jdk1.6.0_17\n" +
            "/opt/java/lost+found\n";

    @Test
    public void testParseDirsAndFiles() throws Exception {
        LsCommand command = new LsCommand(new Server());
        String[] dirs = command.parse(LS_OUTPUT, "/opt/java");
        assertEquals(6, dirs.length);
        assertEquals("jbossx", dirs[0]);
        assertEquals("lost+found", dirs[5]);
        assertEquals(0, command.parse("", "/some/path").length);

    }
}
