package org.xendan.logmonitor.web.read.parse;

import java.util.regex.Matcher;

class NumberParser extends UnitParser<Integer> {

    public NumberParser(String patternStr) {
        super(patternStr);
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
