package org.xendan.logmonitor.web.read.parse;

import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.regex.Matcher;

public class DateParser extends UnitParser<LocalDateTime> {
    public static final String DEFAULT_FORMAT = "yyyy-MM-dd HH:mm:ss,SSS";
    private static final String ABSOLUTE_PATTERN_STR = "HH:mm:ss,SSS";
    //TODO, check definition
    private static final String ABSOLUTE = "ABSOLUTE";
    private static final String ISO8601 = "ISO8601";
    private DateTimeFormatter dateFormatter;

    public DateParser() {
        super("d(\\\\\\{(.+?)\\\\\\})?");
    }

    @Override
    public LocalDateTime toValue(String string) {
        return dateFormatter.parseDateTime(string).toLocalDateTime();
    }

    @Override
    protected String toRegExp(Matcher matcher) {
        String formatAsString = matcher.group(2);
        if (formatAsString == null || ISO8601.equals(formatAsString)) {
            formatAsString = DEFAULT_FORMAT;
        }
        formatAsString = getDatePatternString(formatAsString);
        dateFormatter = DateTimeFormat.forPattern(formatAsString);
        return formatAsString.replaceAll("[yMdHmsS]", "[0-9]");
    }

    private String getDatePatternString(String formatAsString) {
        if (ABSOLUTE.equals(formatAsString)) {
            return ABSOLUTE_PATTERN_STR;
        }
        return formatAsString;
    }


    public String getDateAsString(String logPattern, LocalDateTime date) {
        Matcher matcher = samplePattern.matcher(logPattern);
        if (matcher.find()) {
            toRegExp(matcher);
            return dateFormatter.print(date);
        }
        return null;
    }
}
