package org.xendan.logmonitor.parser;

import org.apache.log4j.Level;
import org.junit.Test;
import org.xendan.logmonitor.model.LogEntry;
import org.xendan.logmonitor.model.MatchPattern;
import org.xendan.logmonitor.model.Matchers;

import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.assertFalse;

/**
 * User: id967161
 * Date: 09/09/13
 */
public class EntryMatcherTest {
    @Test
    public void test_match() throws Exception {
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

    public static EntryMatcher createInfoMatchers() {
        Matchers matchers = new Matchers();
        MatchPattern matcher = new MatchPattern();
        matcher.setLevel(Level.INFO.toString());
        matcher.setError(true);
        matchers.getMatchers().add(matcher);
        return new EntryMatcher(matchers);
    }
}
