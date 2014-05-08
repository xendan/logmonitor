package org.xendan.logmonitor.web.read.parse;

import java.util.regex.Matcher;

class CallerParser extends UnitParser<String> {

    public CallerParser() {
        super("[.\\d-]*C", true);
    }

    @Override
    public String toValue(String string) {
        return string;
    }

    @Override
    protected String toRegExp(Matcher matcher) {
        return "\\s*?[^\\s]*\\s*?";
    }

}
