package org.xendan.logmonitor.parser;

import org.joda.time.LocalDateTime;
import org.xendan.logmonitor.model.Environment;
import org.xendan.logmonitor.model.LogEntry;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogParser {


    private static final String NEW_LINE = System.getProperty("line.separator");

    private final Pattern regexPattern;

    private final UnitParser<String> callerParser = new CallerParser();
    private final UnitParser<LocalDateTime> dateParser = new DateParser();
    private final UnitParser<String> levelParser = new LevelParser();
    private final UnitParser<String> messageParser = new SimpleParser("m");
    private final UnitParser<String> categoryParser = new SimpleParser("c", true);
    private final UnitParser<Integer> lineNumberParser = new LineNumberParser();

    private final UnitParser<?>[] allParsers = {callerParser, dateParser, levelParser, messageParser, categoryParser, lineNumberParser};
    private final List<UnitParser<?>> activeParsers = new ArrayList<UnitParser<?>>();

    private final List<LogEntry> entries = new ArrayList<LogEntry>();
    private final String pattern;
    private final EntryMatcher entryMatcher;
    private final LocalDateTime since;
    private LogEntry lastEntry;

    public LogParser(LocalDateTime since, String pattern, Environment environment) {
        this(since, pattern, new EntryMatcher(environment));
    }

    public LogParser(LocalDateTime since, String pattern, EntryMatcher entryMatcher) {
        this.since = since;
        this.entryMatcher = entryMatcher;
        this.pattern = slashRegexpSymbols(pattern.replace("%n", ""));
        regexPattern = getRegexPattern();
    }

    private String slashRegexpSymbols(String pattern) {
        return PatternUtils.simpleToRegexp(pattern);
        /*
        //Although {} is regexp special, it can be used in log4j, so should not be slashed
        Matcher patternMatcher = PATTERN_MATCHER.matcher(pattern);
        List<String> notSpecialPatterns = new ArrayList<String>();
        List<String> specialPatterns = new ArrayList<String>();
        int index = 0;
        while (patternMatcher.find()) {
            notSpecialPatterns.add(pattern.substring(index, patternMatcher.start()));
            specialPatterns.add(patternMatcher.group());
            index = patternMatcher.end();
        }
        notSpecialPatterns.add(pattern.substring(index));
        List<String> notSpecialSlashed = new ArrayList<String>(notSpecialPatterns.size());
        for (String notSpecialPattern : notSpecialPatterns) {
            notSpecialSlashed.add(PatternUtils.simpleToRegexp(notSpecialPattern));
        }
        String result = "";
        int i;
        for (i= 0; i < specialPatterns.size(); i++) {
           result += notSpecialSlashed.get(i);
           result += specialPatterns.get(i);
        }
        if (i + 1 < specialPatterns.size()) {
            result += specialPatterns.get(i + 1);
        }
        return result;    */
    }


    private Pattern getRegexPattern() {
        initActiveParsers(pattern);
        return Pattern.compile(buildRegexPattern());
    }

    private String buildRegexPattern() {
        String resultPattern = pattern;
        for (UnitParser<?> parser : activeParsers) {
            resultPattern = parser.replaceInPattern(resultPattern);
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
        for (UnitParser<?> parser : allParsers) {
            int start = parser.getStart(pattern);
            if (start != -1) {
                parserMap.put(start, parser);
            }
        }
        return parserMap;
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
        LogEntry newEntry = createEntry(matcher);
        if ((newEntry.getDate() == null || since ==null || newEntry.getDate().isAfter(since)) && entryMatcher.match(newEntry)) {
            entries.add(newEntry);
            lastEntry = newEntry;
        } else  {
            lastEntry = null;
        }
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


    public void clear() {
        entries.clear();
    }
}
