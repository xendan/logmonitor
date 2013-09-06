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
    
    public String replaceInPattern(String pattern, boolean forJava) {
        return replaceInPattern(pattern, forJava, null);
    }

    private String replaceInPattern(String pattern, boolean forJava, EntryMatcher entryMatcher) {
        Matcher matcher = samplePattern.matcher(pattern);
        boolean needBrackets = needBrackets(forJava);
        if (matcher.find()) {
            String basePart = (entryMatcher == null) ? toRegExp(matcher) : getRegexpForEntryMatcher(entryMatcher, matcher);
            String left = needBrackets ? "(" : "";
            String right = needBrackets ? ")" : "";
            return pattern.substring(0, matcher.start())
                    + left + getBasePatternPart(basePart, forJava) + right
                    + pattern.substring(matcher.end());
        }
        return pattern;
    }

    protected boolean needBrackets(boolean forPython) {
        return forPython;
    }

    protected String getBasePatternPart(String basePart, boolean forJava) {
        return basePart;
    }

    protected String getRegexpForEntryMatcher(EntryMatcher entryMatcher, Matcher matcher) {
        return toRegExp(matcher);
    }

    public String replaceInPatternForMatcher(String resultPattern, EntryMatcher entryMatcher) {
        return replaceInPattern(resultPattern, false, entryMatcher);
    }

    public abstract V toValue(String string);

    protected abstract String toRegExp(Matcher matcher);

}
