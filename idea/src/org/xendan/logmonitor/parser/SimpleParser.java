package org.xendan.logmonitor.parser;

import java.util.regex.Matcher;

class SimpleParser extends UnitParser<String> {

    private boolean canBeEmpty;

    public SimpleParser(String log4jPattern) {
        super(log4jPattern);
    }

    public static SimpleParser createWithCurlyBraces(String log4jPattern) {
        return new SimpleParser(log4jPattern, true);
    }

    public static SimpleParser createCanBeEmpty(String log4jPattern) {
        SimpleParser parser = new SimpleParser(log4jPattern);
        parser.canBeEmpty = true;
        return parser;
    }

    private SimpleParser(String samplePatternStr, boolean addCurlBraces) {
        super(samplePatternStr, addCurlBraces);
    }

    @Override
    public String toValue(String string) {
        return string;
    }

    @Override
    protected String toRegExp(Matcher matcher) {
        return "." + (canBeEmpty ? "*" :"+");
    }
}
