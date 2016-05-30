package org.xendan.logmonitor.web.read.parse;

import java.util.regex.Matcher;

class SimpleParser extends UnitParser<String> {

    private boolean canBeEmpty;

    public SimpleParser(String name, char letter) {
        super(name, String.valueOf(letter));
    }

    public static SimpleParser createCanBeEmpty(String name, char letter) {
        SimpleParser parser = new SimpleParser(name, letter);
        parser.canBeEmpty = true;
        return parser;
    }

    @Override
    protected String buildRegexpNoSpaces(Matcher matcher) {
        return "." + (canBeEmpty ? "*" : "+");
    }

}
