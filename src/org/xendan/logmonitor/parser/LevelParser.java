package org.xendan.logmonitor.parser;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.xendan.logmonitor.model.EntryMatcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;

public class LevelParser extends UnitParser<String> {


    private static final Level[] ALL_LEVELS = {Level.DEBUG, Level.ERROR, Level.FATAL, Level.INFO, Level.TRACE, Level.WARN};

    public LevelParser() {
        //TODO: parse number
        super("[.\\d-]*p");
    }

    @Override
    public String toValue(String string) {
        return string.trim();
    }

    @Override
    protected String toRegExp(Matcher matcher) {
        return addSpaces(Arrays.asList(ALL_LEVELS));
    }

    private String addSpaces(List<Level> levels) {
        return "\\s*?(" + StringUtils.join(levels, "|") + ")\\s*?";
    }

    @Override
    protected String getRegexpForEntryMatcher(EntryMatcher entryMatcher, Matcher matcher) {
        if (StringUtils.isEmpty(entryMatcher.getLevel())) {
            return super.getRegexpForEntryMatcher(entryMatcher, matcher);
        }
        return addSpaces(availableLevels(entryMatcher.getLevel()));
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
