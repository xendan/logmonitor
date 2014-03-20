package org.xendan.logmonitor.parser;

import com.jgoodies.binding.beans.BeanUtils;
import org.joda.time.LocalDateTime;
import org.xendan.logmonitor.model.Environment;
import org.xendan.logmonitor.model.LogEntry;

import java.beans.PropertyDescriptor;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogParser {


    private static final String NEW_LINE = System.getProperty("line.separator");

    private final Pattern regexPattern;

    private final static Map<String, UnitParser<?>> ALL_PARSERS = createParsersMap();

    private static Map<String, UnitParser<?>> createParsersMap() {
        Map<String, UnitParser<?>> map = new HashMap<String, UnitParser<?>>();
        //C
        map.put("caller", new CallerParser());
        //d
        map.put("date", new DateParser());
        //p
        map.put("level", new LevelParser());
        //m
        map.put("message", new SimpleParser("m"));
        //t
        map.put("thread", new SimpleParser("t"));
        //c
        map.put("category", SimpleParser.createWithCurlyBraces("c"));
        //L
        map.put("lineNumber", new NumberParser("L"));
        //F
        map.put("fileName", new NumberParser("F"));
        //l
        map.put("locationInformation", new SimpleParser("l"));
        //M
        map.put("methodName", new SimpleParser("M"));
        //r
        map.put("elapsedTime", new SimpleParser("r"));
        //x
        map.put("ndc", SimpleParser.createCanBeEmpty("x"));
        //X
        map.put("mdc", new SimpleParser("X"));
        return map;
    }


    private final List<UnitParser<?>> activeParsers = new ArrayList<UnitParser<?>>();

    private final List<LogEntry> entries = new ArrayList<LogEntry>();
    private final String pattern;
    private final EntryMatcher entryMatcher;
    private final LocalDateTime since;
    private LogEntry lastEntry;
    private boolean somethingWasFound;
    private LocalDateTime lastTime;

    public LogParser(String pattern, Environment environment) {
        this(environment.getLastUpdate(), pattern, new EntryMatcher(environment));
    }

    public LogParser(LocalDateTime since, String pattern, EntryMatcher entryMatcher) {
        this.since = since;
        this.entryMatcher = entryMatcher;
        this.pattern = PatternUtils.simpleToRegexp(pattern.replace("%n", ""));
        regexPattern = getRegexPattern();
    }

    private Pattern getRegexPattern() {
        initActiveParsers(pattern);
        return Pattern.compile(buildRegexPattern(true));
    }

    private String buildRegexPattern(boolean useParentheses) {
        String resultPattern = pattern;
        for (UnitParser<?> parser : activeParsers) {
            resultPattern = parser.replaceInPattern(resultPattern, useParentheses);
        }
        return resultPattern;
    }

    private void initActiveParsers(String pattern) {
        Map<Integer, UnitParser<?>> parserMapStarts = getParserMap(pattern);
        List<Integer> sortedStarts = new ArrayList<Integer>(parserMapStarts.keySet());
        Collections.sort(sortedStarts);
        for (Integer start : sortedStarts) {
            activeParsers.add(parserMapStarts.get(start));
        }
    }

    private Map<Integer, UnitParser<?>> getParserMap(String pattern) {
        Map<Integer, UnitParser<?>> parserMap = new HashMap<Integer, UnitParser<?>>();
        for (UnitParser<?> parser : ALL_PARSERS.values()) {
            int start = parser.getStart(pattern);
            if (start != -1) {
                parserMap.put(start, parser);
            }
        }
        return parserMap;
    }

    public String getRegExpStr() {
        return buildRegexPattern(false);
    }

    public void addString(String log) {
        Matcher matcher = regexPattern.matcher(log);
        if (!matcher.find()) {
            if (lastEntry == null) {
                return;
            }
            lastEntry.setMessage(lastEntry.getMessage() + NEW_LINE + log);
            return;
        }
        somethingWasFound = true;
        LogEntry newEntry = createEntry(matcher);
        lastTime = newEntry.getDate();
        if ((newEntry.getDate() == null || since == null || newEntry.getDate().isAfter(since)) && entryMatcher.match(newEntry)) {
            entries.add(newEntry);
            lastEntry = newEntry;
        } else {
            lastEntry = null;
        }
    }

    private LogEntry createEntry(Matcher matcher) {
        LogEntry logEntry = new LogEntry();
        for (Map.Entry<String, UnitParser<?>> entry : ALL_PARSERS.entrySet()) {
            try {
                PropertyDescriptor descriptor = BeanUtils.getPropertyDescriptor(LogEntry.class, entry.getKey());
                BeanUtils.setValue(logEntry, descriptor, getValue(matcher, entry.getValue()));
            } catch (Exception e) {
                throw new IllegalArgumentException("Error setting parser property " , e);
            }

        }
        return logEntry;
    }

    /**
     * @return found entries, if pattern didn't match provided string, return null
     */
    public List<LogEntry> getEntries() {
        if (somethingWasFound) {
            return entries;
        }
        return null;
    }

    private <V> V getValue(Matcher matcher, UnitParser<V> parser) {
        if (activeParsers.contains(parser)) {
            return parser.toValue(matcher.group(activeParsers.indexOf(parser) + 1));
        }
        return null;
    }


    public void clear() {
        entries.clear();
    }

    public LocalDateTime getLastTime() {
        return lastTime;
    }
}
