package org.xendan.logmonitor.parser;

import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

/**
 * User: id967161
 * Date: 15/10/13
 */
public class PatternUtilsTest {
    @Test
    public void testSimpleToRegexp() throws Exception {
        assertEquals("\\[A\\]B\\|C\\\\D", PatternUtils.simpleToRegexp("[A]B|C\\D"));
    }

    @Test
    public void testRegexToSimple() throws Exception {
       assertEquals("[A]B[CDE$F", PatternUtils.regexToSimple("\\[A\\]B\\[CDE\\$F"));
    }
}
