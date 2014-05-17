package org.xendan.logmonitor.web.read.parse;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PatternUtilsTest {
    @Test
    public void testSimpleToRegexp() throws Exception {
        assertEquals("\\[A\\]B\\|C\\\\D", PatternUtils.simpleToRegexp("[A]B|C\\D"));
    }

    @Test
    public void testRegexToSimple() throws Exception {
        assertEquals("[A]B[CDE$F", PatternUtils.regexToSimple("\\[A\\]B\\[CDE\\$F"));
    }

    @Test
    public void testSimpleToRegexpInJunitPattern() throws Exception {
        assertEquals("%d \\[%7r\\] %6p - %30.30c - %m ", PatternUtils.simpleToRegexp("%d [%7r] %6p - %30.30c - %m ", true));


    }
}
