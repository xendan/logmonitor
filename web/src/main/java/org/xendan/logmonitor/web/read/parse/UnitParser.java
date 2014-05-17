package org.xendan.logmonitor.web.read.parse;

import org.xendan.logmonitor.model.MatchConfig;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class UnitParser<V> {

    protected Pattern samplePattern;
    public UnitParser(String samplePatternStr) {
        samplePattern = Pattern.compile("%[\\.\\d-]*" + decorateSamplePatternString(samplePatternStr));
    }

    protected String decorateSamplePatternString(String samplePatternStr) {
        return samplePatternStr;
    }


    public int getStartPosition(String pattern) {
        Matcher matcher = samplePattern.matcher(pattern);
        if (matcher.find()) {
            return matcher.start();
        }
        return -1;
    }

    public String replaceInPattern(String pattern, boolean useParentheses) {
        Matcher matcher = samplePattern.matcher(pattern);
        if (matcher.find()) {
            return pattern.substring(0, matcher.start())
                    + (useParentheses ? "(" : "") + toRegExp(matcher) + (useParentheses ? ")" : "")
                    + pattern.substring(matcher.end());
        }
        return pattern;
    }

    protected String surroundSpaces(String meaningValue) {
        return "\\s*" + meaningValue + "\\s*";
    }

    protected String getRegexpForEntryMatcher(MatchConfig entryMatcher, Matcher matcher) {
        return toRegExp(matcher);
    }

    protected String toRegExp(Matcher matcher) {
        return surroundSpaces(buildRegexpNoSpaces(matcher));
    }

    protected abstract V toValue(String string);

    protected abstract String buildRegexpNoSpaces(Matcher matcher);


    public V getValue(String group) {
        return toValue(group.trim());
    }
}
