package org.xendan.logmonitor.parser;

import java.util.regex.Matcher;

class SimpleParser extends UnitParser<String> {

    public SimpleParser(String log4jPattern) {
        super(log4jPattern);
    }

    public SimpleParser(String samplePatternStr, boolean addCurlBraces) {
        super(samplePatternStr, addCurlBraces);
    }

    @Override
    public String toValue(String string) {
        return string;
    }

    @Override
    protected String toRegExp(Matcher matcher) {
        return ".+";
    }
}
