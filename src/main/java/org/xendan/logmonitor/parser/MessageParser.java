package org.xendan.logmonitor.parser;

import java.util.regex.Matcher;

class MessageParser extends UnitParser<String> {

    public MessageParser() {
        super("%m");
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
