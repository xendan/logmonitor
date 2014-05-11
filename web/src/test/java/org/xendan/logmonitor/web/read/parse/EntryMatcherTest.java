package org.xendan.logmonitor.web.read.parse;

import org.apache.log4j.Level;
import org.junit.Test;
import org.xendan.logmonitor.model.Environment;
import org.xendan.logmonitor.model.LogEntry;
import org.xendan.logmonitor.model.MatchConfig;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

/**
 * User: id967161
 * Date: 09/09/13
 */
public class EntryMatcherTest {
    @Test
    public void test_match_level() throws Exception {
        EntryMatcher mathcer = createInfoMatchers();

        LogEntry entry = new LogEntry();
        entry.setLevel(Level.WARN.toString());

        assertTrue(mathcer.match(entry));

        entry = new LogEntry();
        entry.setLevel(Level.INFO.toString());

        assertTrue(mathcer.match(entry));
        entry = new LogEntry();

        entry.setLevel(Level.DEBUG.toString());

        assertFalse(mathcer.match(entry));
    }

    @Test
    public void test_message() throws Exception {
        MatchConfig config = new MatchConfig();
        config.setMessage("Hallo");
        EntryMatcher matcher = new EntryMatcher(createEnv(config));

        assertTrue("Expect full match", matcher.match(createEntry("Hallo")));
        assertTrue("Expect contains match", matcher.match(createEntry("***Hallo**")));
        assertFalse(matcher.match(createEntry("***Hll**")));
    }

    private static Environment createEnv(MatchConfig config) {
        Environment env = new Environment();
        env.getMatchConfigs().add(config);
        return env;
    }

    private LogEntry createEntry(String message) {
        LogEntry entry = new LogEntry();
        entry.setMessage(message);
        return entry;
    }

    public static EntryMatcher createInfoMatchers() {
        MatchConfig matcher = new MatchConfig();
        matcher.setLevel(Level.INFO.toString());
        matcher.setMessage("");
        return new EntryMatcher(createEnv(matcher));
    }
}
