package org.xendan.logmonitor.parser;

import java.util.regex.Matcher;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

class DateParser extends UnitParser<DateTime> {

    private static final String ISO8601 = "ISO8601";
    private DateTimeFormatter dateFormatter;

    public DateParser() {
        super("%d\\{(.+?)\\}");
    }

    @Override
    public DateTime toValue(String string) {
        return dateFormatter.parseDateTime(string);
    }

    @Override
    protected String toRegExp(Matcher matcher) {
        String formatAsString = matcher.group(1);
        dateFormatter = getDateFormatter(formatAsString);
        return formatAsString.replaceAll("[yMdHmsS]", "\\\\d");
    }
    
    private DateTimeFormatter getDateFormatter(String dateFormat) {
        if (ISO8601.equals(dateFormat)) {
            return ISODateTimeFormat.dateTimeParser();
        }
        return DateTimeFormat.forPattern(dateFormat);
    }

}
