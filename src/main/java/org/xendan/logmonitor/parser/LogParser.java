package org.xendan.logmonitor.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.time.DateTime;
import org.xendan.logmonitor.model.Level;
import org.xendan.logmonitor.model.LogEntry;

public class LogParser {

    private final String pattern;
    private final Pattern regexPattern;

    private final UnitParser<String> callerParser = new CallerParser();
    private final UnitParser<DateTime> dateParser = new DateParser();
    private final UnitParser<Level> levelParser = new LevelParser();
    private final UnitParser<String> messageParser = new MessageParser();

    private final UnitParser<?>[] allParsers = {callerParser, dateParser, levelParser, messageParser};
    private final List<UnitParser<?>> activeParsers = new ArrayList<UnitParser<?>>();
    private static final String[] REGEX_SPECIAL = {"\\", "[","]", "|", ".", "?", "+", "*", "(", ")"};

    public LogParser(String pattern) {
        this.pattern = pattern;
        regexPattern = getRegexPattern(pattern);
    }

    private Pattern getRegexPattern(String pattern) {
        String resultPattern = replaceSpecial(pattern);
        initActiveParsers(pattern);
        for (UnitParser<?> parser : activeParsers) {
            resultPattern = parser.replaceInPattern(resultPattern);
        }
        resultPattern = resultPattern.replace("%n", "");
        return Pattern.compile(resultPattern);
    }

    private String replaceSpecial(String pattern) {
        String resultPattern = pattern;
        for (String element : REGEX_SPECIAL) {
            resultPattern = resultPattern.replace(element, "\\" + element);
        }
        return resultPattern;
    }

    private void initActiveParsers(String pattern) {
        Map<Integer, UnitParser<?>> parserMap = getParserMap(pattern);
        ArrayList<Integer> sortedPositions = new ArrayList<Integer>(parserMap.keySet());
        Collections.sort(sortedPositions);
        for (Integer position : sortedPositions) {
            if (position != -1) {
                activeParsers.add(parserMap.get(position));
            }
        }
    }

    private Map<Integer, UnitParser<?>> getParserMap(String pattern) {
        Map<Integer, UnitParser<?>> parserMap = new HashMap<Integer, UnitParser<?>>();
        for (UnitParser<?> parser : allParsers) {
            parserMap.put(parser.getStart(pattern), parser);
        }
        return parserMap;
    }

    public LogEntry parse(String log) {
        Matcher matcher = regexPattern.matcher(log);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Log " + log + " doesn't match " + pattern + ", that is " + regexPattern);
        }
        LogEntry entry = new LogEntry();
        entry.setCaller(getValue(matcher, callerParser));
        entry.setDate(getValue(matcher, dateParser));
        entry.setLevel(getValue(matcher, levelParser));
        entry.setMessage(getValue(matcher, messageParser));
        return entry;
    }

    private <V> V getValue(Matcher matcher, UnitParser<V> parser) {
        if (activeParsers.contains(parser)) {
            return parser.toValue(matcher.group(activeParsers.indexOf(parser) + 1));
        }
        return null;
    }

}
