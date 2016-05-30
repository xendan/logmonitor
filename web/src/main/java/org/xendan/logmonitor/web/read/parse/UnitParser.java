package org.xendan.logmonitor.web.read.parse;

import org.apache.commons.beanutils.BeanUtils;
import org.xendan.logmonitor.model.LogEntry;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class UnitParser<V> {

    protected Pattern unitPattern;
    protected String name;

    private static final List<String> LOG_ENTRY_FIELDS = Arrays.asList("level", "message");


    public UnitParser(String name, String samplePatternStr) {
        unitPattern = Pattern.compile(getPatternPrefix() + samplePatternStr + getPatternSuffix());
        this.name = name;
    }

    public String getName() {
        return name;
    }

    protected String getPatternSuffix() {
        return "";
    }

    private String getPatternPrefix() {
        return "%[\\.\\d-]*";
    }

    public boolean isPresentInPattern(String allPattern) {
        return unitPattern.matcher(allPattern).find();
    }

    public String replaceInPattern(String pattern) {
        Matcher matcher = unitPattern.matcher(pattern);
        if (matcher.find()) {
            return pattern.substring(0, matcher.start())
                    + "\\s*(?<" + name + ">" + toRegExp(matcher) + ")\\s*"
                    + pattern.substring(matcher.end());
        }
        return pattern;
    }

    protected String surroundSpaceRegexp(String meaningValue) {
        return "\\s*" + meaningValue + "\\s*";
    }

//    protected String getRegexpForEntryMatcher(MatchConfig entryMatcher, Matcher matcher) {
//        return toRegExp(matcher);
//    }

    protected String toRegExp(Matcher matcher) {
        return surroundSpaceRegexp(buildRegexpNoSpaces(matcher));
    }

    protected abstract String buildRegexpNoSpaces(Matcher matcher);

    public void setValueToEntry(String value, LogEntry logEntry) {
        if (LOG_ENTRY_FIELDS.contains(name)) {
            try {
                BeanUtils.setProperty(logEntry, name, value);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new IllegalArgumentException("Can't set bean property", e);
            }
        } else {
            logEntry.getProperties().put(name, value);
        }
    }
}
