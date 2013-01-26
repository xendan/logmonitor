package org.xendan.logmonitor.parser;

import java.util.regex.Matcher;

class SimpleParser extends UnitParser<String> {

    public SimpleParser(String log4jPattern) {
        super(log4jPattern);
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
