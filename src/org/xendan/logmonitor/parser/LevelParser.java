package org.xendan.logmonitor.parser;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.xendan.logmonitor.model.MatchPattern;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;

public class LevelParser extends UnitParser<String> {
    private static final Level[] ALL_LEVELS = {Level.DEBUG, Level.ERROR, Level.FATAL, Level.INFO, Level.TRACE, Level.WARN};

    public LevelParser() {
        super("([.\\d-]*)p");
    }

    @Override
    public String toValue(String string) {
        return string.trim();
    }

    @Override
    protected String toRegExp(Matcher matcher) {
        return addSpaces(Arrays.asList(ALL_LEVELS), matcher);
    }

    private String addSpaces(List<Level> levels, Matcher matcher) {
        return StringUtils.join(addShift(levels, getShift(matcher)), "|");
    }

    private int getShift(Matcher matcher) {
        String shiftStr = matcher.group(1);
        if (StringUtils.isNotEmpty(shiftStr)) {
            return Integer.valueOf(shiftStr);
        }
        return 0;
    }

    private List<String> addShift(List<Level> levels, int shift) {
        List<String> shiftedLevels = new ArrayList<String>();
        for (Level level : levels) {
            String leftShift = (shift > 0) ? spaces(5 - level.toString().length()) : "";
            String rightShift = (shift < 0) ? spaces(-level.toString().length() - shift) : "";
            shiftedLevels.add(leftShift + level.toString() + rightShift);
        }
        return shiftedLevels;

    }

    private String spaces(int i) {
        StringBuilder builder = new StringBuilder();
        for (int j = 0; j < i; j++) {
            builder.append(" ");
        }
        return builder.toString();
    }

    @Override
    protected String getRegexpForEntryMatcher(MatchPattern entryMatcher, Matcher matcher) {
        if (StringUtils.isEmpty(entryMatcher.getLevel())) {
            return super.getRegexpForEntryMatcher(entryMatcher, matcher);
        }
        return addSpaces(availableLevels(entryMatcher.getLevel()), matcher);
    }

    private List<Level> availableLevels(String levelStr) {
        Level configLevel = Level.toLevel(levelStr);
        List<Level> matchLevels = new ArrayList<Level>();
        for (Level level : ALL_LEVELS) {
            if (level.isGreaterOrEqual(configLevel)) {
                matchLevels.add(level);
            }
        }
        return matchLevels;
    }
}
