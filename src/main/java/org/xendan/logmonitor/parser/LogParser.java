package org.xendan.logmonitor.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xendan.logmonitor.model.Level;
import org.xendan.logmonitor.model.LogEntry;

public class LogParser {

    private static final String[] REGEX_SPECIAL = {"\\", "[","]", "|", ".", "?", "+", "*", "(", ")"};
    private static final String NEW_LINE = System.getProperty("line.separator");
    
    private static final Logger logger = LoggerFactory.getLogger(LogParser.class);
    
    private final Pattern regexPattern;

    private final UnitParser<String> callerParser = new CallerParser();
    private final UnitParser<DateTime> dateParser = new DateParser();
    private final UnitParser<Level> levelParser = new LevelParser();
    private final UnitParser<String> messageParser = new SimpleParser("m");
    private final UnitParser<String> categoryParser = new SimpleParser("c");
    private final UnitParser<Integer> lineNumberParser = new LineNumberParser();

    private final UnitParser<?>[] allParsers = {callerParser, dateParser, levelParser, messageParser, categoryParser, lineNumberParser};
    private final List<UnitParser<?>> activeParsers = new ArrayList<UnitParser<?>>();
    
    private final List<LogEntry> entries = new ArrayList<LogEntry>();
    
    public LogParser(String pattern) {
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
        List<Integer> sortedPositions = new ArrayList<Integer>(parserMap.keySet());
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

    public void addString(String log) {
        Matcher matcher = regexPattern.matcher(log);
        if (!matcher.find()) {
            if (entries.isEmpty()) {
                logger.warn("Message with no previous entry " + log);
                return;
            }
            LogEntry last = entries.get(entries.size() - 1);
            last.setMessage(last.getMessage() + NEW_LINE + log);
            return;
        }
        entries.add(createEntry(matcher));
    }

    private LogEntry createEntry(Matcher matcher) {
        LogEntry entry = new LogEntry();
        entry.setCaller(getValue(matcher, callerParser));
        entry.setDate(getValue(matcher, dateParser));
        entry.setLevel(getValue(matcher, levelParser));
        entry.setMessage(getValue(matcher, messageParser));
        entry.setCategory(getValue(matcher, categoryParser));
        entry.setLineNumber(getValue(matcher, lineNumberParser));
        return entry;
    }
    
    public List<LogEntry> getEntries() {
        return entries;
    }

    private <V> V getValue(Matcher matcher, UnitParser<V> parser) {
        if (activeParsers.contains(parser)) {
            return parser.toValue(matcher.group(activeParsers.indexOf(parser) + 1));
        }
        return null;
    }

}
