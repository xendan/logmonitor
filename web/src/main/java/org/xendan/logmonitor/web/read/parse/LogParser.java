package org.xendan.logmonitor.web.read.parse;

import org.joda.time.LocalDateTime;
import org.xendan.logmonitor.model.Environment;
import org.xendan.logmonitor.model.LogEntry;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class LogParser {


    private static final String NEW_LINE = System.getProperty("line.separator");

    private final Pattern regexPattern;

    private final List<UnitParser<?>> parsers;

    private final List<LogEntry> entries = new ArrayList<>();
    private final EntryMatcher entryMatcher;
    private final LocalDateTime since;
    private LogEntry lastEntry;
    private boolean somethingWasFound;
    private LocalDateTime lastTime;

    public LogParser(String pattern, Environment environment) {
        this(environment.getLastUpdate(), pattern, new EntryMatcher(environment));
    }

    public LogParser(LocalDateTime since, String logPattern, EntryMatcher entryMatcher) {
        this.since = since;
        this.entryMatcher = entryMatcher;
        String resultPattern =  PatternUtils.simpleToRegexp(logPattern.replace("%n", "")
                .replace("\\r", "")
                .replace("\\n", ""), true);

        parsers = ParserFactory.getParcers(resultPattern);
        String regexPattern = buildRegexPattern(resultPattern);
        this.regexPattern = Pattern.compile(regexPattern);
    }

    private String buildRegexPattern(String pattern) {
        for (UnitParser<?> parser : parsers) {
            pattern = parser.replaceInPattern(pattern);
        }
        return pattern;
    }

    public void addString(String log) {
        Matcher matcher = regexPattern.matcher(log);
        if (!matcher.matches()) {
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
        for (UnitParser<?> parser : parsers) {
            parser.setValueToEntry(matcher.group(parser.getName()), logEntry);
        }
        return logEntry;
    }

    /**
     * @return found entries, if log4jPreparedPattern didn't match provided string, return null
     */
    public List<LogEntry> getEntries() {
        if (somethingWasFound) {
            return entries;
        }
        return null;
    }

    public void clear() {
        entries.clear();
    }

    public LocalDateTime getLastTime() {
        return lastTime;
    }

    public List<String> getVisibleFields() {
        return parsers.stream().map(UnitParser::getName).collect(Collectors.toList());

    }
}
