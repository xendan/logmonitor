package org.xendan.logmonitor.web.read.parse;

import org.xendan.logmonitor.model.MatchConfig;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class UnitParser<V> {

    protected Pattern samplePattern;
    
    public UnitParser(String samplePatternStr) {
        this(samplePatternStr, false);
    }

    public UnitParser(String samplePatternStr, boolean addCurlBraces) {
        if (addCurlBraces) {
            samplePatternStr += "(\\\\\\{.+\\\\\\})?";
        }
        samplePattern = Pattern.compile("%" + samplePatternStr);
    }

    public int getStart(String pattern) {
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




    protected String getRegexpForEntryMatcher(MatchConfig entryMatcher, Matcher matcher) {
        return toRegExp(matcher);
    }

    public abstract V toValue(String string);

    protected abstract String toRegExp(Matcher matcher);


}
