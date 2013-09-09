package org.xendan.logmonitor.parser;

import org.junit.Test;

import java.util.regex.Pattern;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

/**
 * User: id967161
 * Date: 06/09/13
 */
public class LevelParserTest {
    @Test
    public void test_match() throws Exception {
        LevelParser parser = new LevelParser();
        String regexpPattern = parser.replaceInPattern("%p");
        assertTrue("Expect simple found" + regexpPattern, Pattern.matches(regexpPattern, "WARN"));
        assertTrue("Expect simple found" + regexpPattern, Pattern.matches(regexpPattern, "ERROR"));
        assertFalse("Expect simple not found" + regexpPattern, Pattern.matches(regexpPattern, " ERROR "));

        regexpPattern = parser.replaceInPattern("%-5p");
        assertTrue("Expect aligned left found " + regexpPattern, Pattern.matches(regexpPattern, "WARN "));
        assertFalse("Expect aligned left not found" + regexpPattern, Pattern.matches(regexpPattern, " WARN"));
        assertTrue("Expect aligned left found" + regexpPattern, Pattern.matches(regexpPattern, "DEBUG"));

        regexpPattern = parser.replaceInPattern("%5p");
        assertTrue("Expect aligned right found" + regexpPattern, Pattern.matches(regexpPattern, " WARN"));
        assertFalse("Expect aligned not right found" + regexpPattern, Pattern.matches(regexpPattern, "WARN "));
        assertTrue("Expect aligned right found" + regexpPattern, Pattern.matches(regexpPattern, "DEBUG"));

    }
}
