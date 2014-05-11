package org.xendan.logmonitor.web.read.parse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.xendan.logmonitor.model.LogEntry;
import org.xendan.logmonitor.model.MatchConfig;

import java.util.regex.Pattern;

/**
 * User: id967161
 * Date: 15/10/13
 */
public class PatternUtils {
    public static final String ALL_GROUP = "(.*)";
    public static final String DOT = ".";
    private static final String[] REGEX_SPECIAL = {"\\", "[", "]", "|", DOT, "?", "+", "*", "(", ")", "{", "}", "$"};

    public static String simpleToRegexp(String pattern) {
        return simpleToRegexp(pattern, false);
    }

    public static String simpleToRegexp(String pattern, boolean ignoreDot) {
        for (String element : REGEX_SPECIAL) {
            if (!ignoreDot || !DOT.equals(element)) {
                pattern = pattern.replace(element, "\\" + element);
            }
        }
        return pattern;
    }

    public static String regexToSimple(String pattern) {
        for (String element : REGEX_SPECIAL) {
            pattern = pattern.replace("\\" + element, element);
        }
        return pattern;
    }

    public static boolean isMatch(LogEntry entry, MatchConfig matchPattern) {
        Level matchLevel = matchPattern.getLevel() == null ? null : Level.toLevel(matchPattern.getLevel());
        Level entryLevel = Level.toLevel(entry.getLevel());
        boolean match = (matchLevel == null) || entryLevel.isGreaterOrEqual(matchLevel);
        return StringUtils.isEmpty(matchPattern.getMessage()) ? match : messageMatch(entry.getMessage(), matchPattern.getMessage());
    }

    private static boolean messageMatch(String message, String messagePattern) {
        return Pattern.compile(messagePattern, Pattern.CASE_INSENSITIVE).matcher(message).find();
    }

    public static String restoreMessage(LogEntry entry, String messagePattern) {
        if (messagePattern.contains(PatternUtils.ALL_GROUP)) {
            return PatternUtils.regexToSimple(messagePattern).replace(PatternUtils.ALL_GROUP, entry.getMessage());
        } else {
            return PatternUtils.regexToSimple(messagePattern);
        }
    }
}
