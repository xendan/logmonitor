package org.xendan.logmonitor.parser;

import java.util.regex.Matcher;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

class DateParser extends UnitParser<DateTime> {
    private static final String ABSOLUTE_PATTERN_STR = "HH:mm:ss,SSS";
    private static final DateTimeFormatter ABSOLUTE_PATTERN = DateTimeFormat.forPattern(ABSOLUTE_PATTERN_STR);
    //TODO, check definition
    private static final String ABSOLUTE = "ABSOLUTE";
    private static final String ISO8601 = "ISO8601";
    private DateTimeFormatter dateFormatter;

    public DateParser() {
        super("d\\{(.+?)\\}");
    }

    @Override
    public DateTime toValue(String string) {
        return dateFormatter.parseDateTime(getDatePatternString(string));
    }

    @Override
    protected String toRegExp(Matcher matcher) {
        String formatAsString = matcher.group(1);
        dateFormatter = getDateFormatter(getDatePatternString(formatAsString));
        return getDatePatternString(formatAsString).replaceAll("[yMdHmsS]", "\\\\d");
    }

    private String getDatePatternString(String formatAsString) {
        if (ABSOLUTE.equals(formatAsString)) {
            return ABSOLUTE_PATTERN_STR;
        }
        //TODO add for ISO
        return formatAsString;
    }
    
    private DateTimeFormatter getDateFormatter(String dateFormat) {
        if (ABSOLUTE.equals(getDatePatternString(dateFormat))) {
            return ABSOLUTE_PATTERN;
        }
        if (ISO8601.equals(getDatePatternString(dateFormat))) {
            return ISODateTimeFormat.dateTimeParser();
        }
        return DateTimeFormat.forPattern(getDatePatternString(dateFormat));
    }

}
