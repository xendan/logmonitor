package org.xendan.logmonitor.web.read.parse;

import java.util.regex.Matcher;

class SimpleParser extends UnitParser<String> {

    private boolean canBeEmpty;

    public SimpleParser(char letter) {
        super(String.valueOf(letter));
    }

    public static SimpleParser createCanBeEmpty(char letter) {
        SimpleParser parser = new SimpleParser(letter);
        parser.canBeEmpty = true;
        return parser;
    }

    @Override
    protected String buildRegexpNoSpaces(Matcher matcher) {
        return "." + (canBeEmpty ? "*" : "+");
    }

    @Override
    protected String toValue(String string) {
        return string;
    }
}
