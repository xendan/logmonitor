package org.xendan.logmonitor.parser;

import java.util.regex.Matcher;

import org.xendan.logmonitor.model.Level;

public class LevelParser extends UnitParser<Level> {

    public LevelParser() {
        super("%[.\\d-]*p");
    }

    @Override
    public Level toValue(String string) {
        return Level.fromString(string.trim());
    }

    @Override
    protected String toRegExp(Matcher matcher) {
        return "\\s*?\\w+\\s*?";
    }
}
