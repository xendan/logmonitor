package org.xendan.logmonitor.web.read.parse;

import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.junit.Test;

import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DateParserTest {
    @Test
    public void test_default_date() throws Exception {
        DateParser dateParser = new DateParser();
        String regexpPattern = dateParser.replaceInPattern("%d", true);
        assertTrue("Expect default ISO8601 date found by patten " + regexpPattern, Pattern.matches(regexpPattern, "2013-09-06 09:30:22,093"));
    }

    @Test
    public void test_to_string() throws Exception {
        DateParser dateParser = new DateParser();
        String dateStr = "2013-09-10 12:40:03,300";
        LocalDateTime date = DateTimeFormat.forPattern(DateParser.DEFAULT_FORMAT).parseDateTime(dateStr).toLocalDateTime();
        assertEquals(dateStr, dateParser.getDateAsString("%d %-5p [%c] %m%n", date));

    }
}
