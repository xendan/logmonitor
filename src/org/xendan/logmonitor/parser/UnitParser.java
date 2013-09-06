package org.xendan.logmonitor.parser;

import org.xendan.logmonitor.model.EntryMatcher;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class UnitParser<V> {

    protected Pattern samplePattern;
    
    public UnitParser(String samplePatternStr) {
        samplePattern = Pattern.compile("%" + samplePatternStr);
    }

    public int getStart(String pattern) {
        Matcher matcher = samplePattern.matcher(pattern);
        if (matcher.find()) {
            return matcher.start();
        }
        return -1;
    }
    
    public String replaceInPattern(String pattern, boolean brackets) {
        return replaceInPattern(pattern, brackets, null);
    }

    private String replaceInPattern(String pattern, boolean brackets, EntryMatcher entryMatcher) {
        Matcher matcher = samplePattern.matcher(pattern);
        boolean addBreakets = needBrackets(brackets);
        if (matcher.find()) {
            String basePart = (entryMatcher == null) ? toRegExp(matcher) : getRegexpForEntryMatcher(entryMatcher, matcher);
            String left = addBreakets ? "(" : "";
            String right = addBreakets ? ")" : "";
            return pattern.substring(0, matcher.start())
                    + left + basePart + right
                    + pattern.substring(matcher.end());
        }
        return pattern;
    }

    protected String getRegexpForEntryMatcher(EntryMatcher entryMatcher, Matcher matcher) {
        return toRegExp(matcher);
    }

    public String replaceInPatternForMatcher(String resultPattern, EntryMatcher entryMatcher) {
        return replaceInPattern(resultPattern, false, entryMatcher);
    }

    protected boolean needBrackets(boolean brackets) {
        return brackets;
    }

    public abstract V toValue(String string);

    protected abstract String toRegExp(Matcher matcher);


    public Integer getGroupsNumber() {
        return 1;
    }
}
