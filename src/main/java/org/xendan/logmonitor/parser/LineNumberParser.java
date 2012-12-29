package org.xendan.logmonitor.parser;

import java.util.regex.Matcher;

class LineNumberParser extends UnitParser<Integer> {

    public LineNumberParser() {
        super("L");
    }

    @Override
    public Integer toValue(String string) {
        return Integer.valueOf(string);
    }

    @Override
    protected String toRegExp(Matcher matcher) {
        return "\\d+";
    }
}
