package org.xendan.logmonitor.parser;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.util.regex.Matcher;

class DateParser extends UnitParser<DateTime> {
    private static final String ABSOLUTE_PATTERN_STR = "HH:mm:ss,SSS";
    private static final DateTimeFormatter ABSOLUTE_PATTERN = DateTimeFormat.forPattern(ABSOLUTE_PATTERN_STR);
    //TODO, check definition
    private static final String ABSOLUTE = "ABSOLUTE";
    private static final String ISO8601 = "ISO8601";
    private DateTimeFormatter dateFormatter;

    public DateParser() {
        super("d(\\{(.+?)\\})?");
    }

    @Override
    public DateTime toValue(String string) {
        return dateFormatter.parseDateTime(getDatePatternString(string));
    }

    @Override
    protected boolean needBrackets(boolean brackets) {
        return true;
    }

    @Override
    protected String toRegExp(Matcher matcher) {
        String formatAsString = matcher.group(2);
        dateFormatter = getDateFormatter(getDatePatternString(formatAsString));
        if (formatAsString == null || ISO8601.equals(formatAsString)) {
            formatAsString = "yyyy-MM-dd HH:mm:ss,SSS";
        }
        return getDatePatternString(formatAsString).replaceAll("[yMdHmsS]", "\\\\d");
    }

    private String getDatePatternString(String formatAsString) {
        if (ABSOLUTE.equals(formatAsString)) {
            return ABSOLUTE_PATTERN_STR;
        }
        if (formatAsString == null) {
            return ISO8601;
        }
        //TODO add for ISO
        return formatAsString;
    }
    
    private DateTimeFormatter getDateFormatter(String dateFormat) {
        String datePatternString = getDatePatternString(dateFormat);
        if (ABSOLUTE.equals(datePatternString)) {
            return ABSOLUTE_PATTERN;
        }
        if (ISO8601.equals(datePatternString)) {
            return ISODateTimeFormat.dateTimeParser();
        }
        return DateTimeFormat.forPattern(datePatternString);
    }

    @Override
    public Integer getGroupsNumber() {
        return 2;
    }
}
