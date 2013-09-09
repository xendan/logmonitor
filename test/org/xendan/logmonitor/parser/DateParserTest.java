package org.xendan.logmonitor.parser;

import org.junit.Test;

import java.util.regex.Pattern;

import static junit.framework.Assert.assertTrue;

/**
 * User: id967161
 * Date: 06/09/13
 */
public class DateParserTest {
    @Test
    public void test_default_date() throws Exception {
        DateParser dateParser = new DateParser();
        String regexpPattern = dateParser.replaceInPattern("%d");
        assertTrue("Expect default ISO8601 date found by patten " + regexpPattern, Pattern.matches(regexpPattern, "2013-09-06 09:30:22,093"));
    }
}
