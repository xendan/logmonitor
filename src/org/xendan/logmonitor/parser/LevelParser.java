package org.xendan.logmonitor.parser;

import org.apache.log4j.Level;

import java.util.regex.Matcher;

public class LevelParser extends UnitParser<Level> {

    public LevelParser() {
        super("[.\\d-]*p");
    }

    @Override
    public Level toValue(String string) {
        return Level.toLevel(string.trim());
    }

    @Override
    protected String toRegExp(Matcher matcher) {
        return "\\s*?\\w+\\s*?";
    }
}
