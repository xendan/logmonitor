package org.xendan.logmonitor.web.read.parse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.xendan.logmonitor.model.MatchConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;

public class LevelParser extends SimpleParser {
    private static final Level[] ALL_LEVELS = {Level.DEBUG, Level.ERROR, Level.FATAL, Level.INFO, Level.TRACE, Level.WARN};

    public LevelParser() {
        super('p');
    }

    private String joinLevels(List<Level> levels) {
        return StringUtils.join(surroundSpaces(levels), "|");
    }

    private List<String> surroundSpaces(List<Level> levels) {
        List<String> withSpaces = new ArrayList<String>(levels.size());
        for (Level level : levels) {
            withSpaces.add(surroundSpaces(level.toString()));
        }
        return withSpaces;
    }

    @Override
    protected String toRegExp(Matcher matcher) {
        return joinLevels(Arrays.asList(ALL_LEVELS));
    }

    @Override
    protected String getRegexpForEntryMatcher(MatchConfig entryMatcher, Matcher matcher) {
        if (StringUtils.isEmpty(entryMatcher.getLevel())) {
            return super.getRegexpForEntryMatcher(entryMatcher, matcher);
        }
        return joinLevels(availableLevels(entryMatcher.getLevel()));
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
