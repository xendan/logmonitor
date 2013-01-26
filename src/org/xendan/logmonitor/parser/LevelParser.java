package org.xendan.logmonitor.parser;

import java.util.regex.Matcher;

public class LevelParser extends UnitParser<String> {

    public LevelParser() {
        super("[.\\d-]*p");
    }

    @Override
    public String toValue(String string) {
        return string.trim();
    }

    @Override
    protected String toRegExp(Matcher matcher) {
        return "\\s*?\\w+\\s*?";
    }
}
