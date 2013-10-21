package org.xendan.logmonitor.parser;

/**
 * User: id967161
 * Date: 15/10/13
 */
public class PatternUtils {
    private static final String[] REGEX_SPECIAL = {"\\", "[", "]", "|", ".", "?", "+", "*", "(", ")", "$"};

    public static String simpleToRegexp(String pattern) {
        for (String element : REGEX_SPECIAL) {
            pattern = pattern.replace(element, "\\" + element);
        }
        return pattern;
    }

    public static String regexToSimple(String pattern) {
        for (String element : REGEX_SPECIAL) {
            pattern = pattern.replace("\\" + element, element);
        }
        return pattern;
    }
}
