package org.xendan.logmonitor.parser;

import org.joda.time.DateTime;
import org.xendan.logmonitor.model.EntryMatcher;
import org.xendan.logmonitor.model.LogEntry;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogParser {

    private static final String[] REGEX_SPECIAL = {"\\", "[","]", "|", ".", "?", "+", "*", "(", ")"};
    private static final String NEW_LINE = System.getProperty("line.separator");
    
    private final Pattern regexPattern;

    private final UnitParser<String> callerParser = new CallerParser();
    private final UnitParser<DateTime> dateParser = new DateParser();
    private final UnitParser<String> levelParser = new LevelParser();
    private final UnitParser<String> messageParser = new SimpleParser("m");
    private final UnitParser<String> categoryParser = new SimpleParser("c");
    private final UnitParser<Integer> lineNumberParser = new LineNumberParser();

    private final UnitParser<?>[] allParsers = {callerParser, dateParser, levelParser, messageParser, categoryParser, lineNumberParser};
    private final List<UnitParser<?>> activeParsers = new ArrayList<UnitParser<?>>();
    
    private final List<LogEntry> entries = new ArrayList<LogEntry>();
    private final String pattern;

    public LogParser(String pattern) {
        this.pattern = replaceSpecial(pattern);
        regexPattern = getRegexPattern();
    }

    private Pattern getRegexPattern() {
        initActiveParsers(pattern);
        return Pattern.compile(buildRegexPattern(true));
    }

    private String buildRegexPattern(boolean forJava) {
        String resultPattern = pattern;
        for (UnitParser<?> parser : activeParsers) {
            resultPattern = parser.replaceInPattern(resultPattern, forJava);
        }
        return resultPattern;
    }

    public String getEntryMatcherPattern(EntryMatcher entryMatcher) {
        String resultPattern = pattern;
        for (UnitParser<?> parser : activeParsers) {
            resultPattern = parser.replaceInPatternForMatcher(resultPattern, entryMatcher);
        }
        return resultPattern;

    }

    private String replaceSpecial(String pattern) {
        String resultPattern = pattern;
        for (String element : REGEX_SPECIAL) {
            resultPattern = resultPattern.replace(element, "\\" + element);
        }
        return resultPattern.replace("%n", "");
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

    public LogEntry addString(String log) {
        Matcher matcher = regexPattern.matcher(log);
        if (!matcher.find()) {
            if (entries.isEmpty()) {
                return null;
            }
            LogEntry last = entries.get(entries.size() - 1);
            last.setMessage(last.getMessage() + NEW_LINE + log);
            return last;
        }
        LogEntry newEntry = createEntry(matcher);
        entries.add(newEntry);
        return newEntry;
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

    /**
     * @return regexp for mathc common string with group for date
     */
    public String getCommonPythonRegexp() {
        return buildRegexPattern(false);
    }

}
