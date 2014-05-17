package org.xendan.logmonitor.web.read.parse;

import org.apache.log4j.Level;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.junit.Test;
import org.xendan.logmonitor.model.Environment;
import org.xendan.logmonitor.model.LogEntry;

import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
public class LogParserTest {

    private static final String MESSAGE = "some message not multiline";

    public static final String LOG_WARN = "2012-09-21 01:12:17,191 WARN  [org.caramba.CarambaContext] " + MESSAGE;
    public static final String LOG_INFO = "2012-09-22 01:12:17,191 INFO  [org.caramba.CarambaContext] " + MESSAGE;
    public static final String LOG_ERROR = "2012-09-23 01:12:17,191 ERROR [org.caramba.CarambaContext] " + MESSAGE;
    private static final String DEBUG_DATE = "2012-09-24 01:12:17,191";
    public static final String LOG_DEBUG = DEBUG_DATE + " DEBUG [org.caramba.CarambaContext] " + MESSAGE;
    private static final String NL = System.getProperty("line.separator");
    public static final String FULL_PATTERN = "%d{yyyy-MM-dd HH:mm:ss,SSS} %-5p [%C]";
    private static final EntryMatcher INFO_MATCHER = EntryMatcherTest.createInfoMatchers();
    public static final LocalDateTime A_WHILE_AGO = defaultFrmtDate("1900-09-22 01:12:17,191");

    @Test
    public void testErrorReportedOnInvalidFileFormat() throws Exception {
        LogParser parser = new LogParser("%d %-5p [%c] %m%n", new Environment());
        parser.addString("something not from patter");
        parser.addString("something not from pattern");
        parser.addString("again something crazy");

        assertNull("pattern doesn't match ", parser.getEntries());

    }

    @Test
    public void test_parse_date() {
        LogEntry entry = singleEntry("%d{yyyy-MM-dd HH:mm:ss,SSS}", "2012-09-28 01:12:17,191");
        assertEquals(28, entry.getDate().getDayOfMonth());
    }

    @Test
    public void test_idea_log() {
        LogEntry entry = singleEntry("%d [%7r] %6p - %30.30c - %m \\n", "2014-04-04 19:53:52,100 [     17]   INFO -        #com.intellij.idea.Main - IDE: IntelliJ IDEA (build #IC-135.480, 21 Mar 2014 00:00) ");
        assertEquals(Level.INFO.toString(), entry.getLevel());
    }

    @Test
    public void test_getDateAsString() throws Exception {
        LogParser parser = new LogParser("%d %-5p [%c] %m%n", new Environment());
        assertEquals("\\s*[0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9] [0-9][0-9]:[0-9][0-9]:[0-9][0-9],[0-9][0-9][0-9]\\s* \\s*DEBUG\\s*|\\s*ERROR\\s*|\\s*FATAL\\s*|\\s*INFO\\s*|\\s*TRACE\\s*|\\s*WARN\\s* \\[\\s*.+\\s*\\] \\s*.+\\s*",
                parser.getRegExpStr());
    }

    @Test
    public void testGetVisibleFields() throws Exception {
        LogParser parser = new LogParser("%d %-5p [%c] %m%n", new Environment());
        List<String> fields = parser.getVisibleFields();
        assertEquals(4, fields.size());
        assertTrue(fields.contains("level"));
        assertTrue(fields.contains("date"));
        assertTrue(fields.contains("category"));
        assertTrue(fields.contains("message"));

    }

    @Test
    public void test_use_case1() throws Exception {
        singleEntry("%d %-5p [%c] %m%n", "2013-09-10 00:00:00,016 INFO  [com.bics.btts.job.ActionRepairTimeJob] ActionRepairTimeJob started : 10/09/2013 00:00");
    }

    @Test
    public void test_thread_pattern() throws Exception {
        singleEntry("%d [%t] %-5p %c %x - %m%n", "2014-03-19 18:00:30,023 [org.springframework.scheduling.quartz.SchedulerFactoryBean#0_Worker-5] INFO  com.bics.btts.service.impl.SlaServiceImpl  - On ticket: 295160 rules executed: none rules not executed: AUTO_INCREASE_ESCALATION,AUTO_MAIL_TICKET_ESCALATION");
    }

    private LogEntry singleEntry(String pattern, String log) {
        EntryMatcher generousMatcher = createGenerousMatcher();
        LogParser parser = new LogParser(A_WHILE_AGO, pattern, generousMatcher);
        parser.addString(log);
        assertNotNull("Entry " + log + "don't match pattern " + pattern, parser.getEntries());
        return parser.getEntries().get(0);
    }


    @Test
    public void test_parse_level() throws Exception {
        LogEntry entry = singleEntry("%-5p", "WARN ");
        assertEquals(Level.WARN.toString(), entry.getLevel());
    }

    @Test
    public void test_clear() throws Exception {
        LogParser parser = new LogParser(A_WHILE_AGO, "%-5p", INFO_MATCHER);
        parser.addString("WARN ");
        parser.addString("DEBUG");
        parser.clear();

        assertTrue("Expect log cleared", parser.getEntries().isEmpty());
        parser.addString("INFO ");

        assertEquals("Expect after clear", 1, parser.getEntries().size());
    }

    @Test
    public void test_parse_caller() throws Exception {
        LogEntry entry = singleEntry("%C", "org.caramba.CarambaContext");

        assertEquals("org.caramba.CarambaContext", entry.getCaller());
    }

    @Test
    public void test_parse_message() throws Exception {
        LogEntry entry = singleEntry("%m", "hello world");

        assertEquals("hello world", entry.getMessage());
    }

    @Test
    public void test_parse_date_level() throws Exception {
        LogEntry entry = singleEntry("%d{yyyy-MM-dd HH:mm:ss,SSS} %-5p", "2012-09-28 01:12:17,191 WARN ");

        assertEquals(28, entry.getDate().getDayOfMonth());
        assertEquals(Level.WARN.toString(), entry.getLevel());
    }

    @Test
    public void test_no_read_before() throws Exception {
        LocalDateTime dateTime = defaultFrmtDate("2012-09-21 01:12:17,191");
        LogParser parser = new LogParser(dateTime, FULL_PATTERN, INFO_MATCHER);
        parser.addString(LOG_WARN);
        parser.addString(LOG_ERROR);
        parser.addString(LOG_INFO);
        parser.addString(LOG_DEBUG);

        assertEquals("Expect only error and info read after defined date", 2, parser.getEntries().size());
    }

    public static LocalDateTime defaultFrmtDate(String text) {
        return DateTimeFormat.forPattern(DateParser.DEFAULT_FORMAT).parseDateTime(text).toLocalDateTime();
    }

    @Test
    public void test_parse_all_no_message() throws Exception {
        LogEntry entry = singleEntry(FULL_PATTERN,
                "2012-09-28 01:12:17,191 WARN  [org.caramba.CarambaContext]");

        assertEquals(28, entry.getDate().getDayOfMonth());
        assertEquals("org.caramba.CarambaContext", entry.getCaller());
        assertEquals(Level.WARN.toString(), entry.getLevel());
    }

    @Test
    public void test_parse_all() throws Exception {
        String message = "some message not multiline";
        LogEntry entry = singleEntry("%d{yyyy-MM-dd HH:mm:ss,SSS} %-5p [%C] %m%n",
                "2012-09-28 01:12:17,191 WARN  [org.caramba.CarambaContext] " + message);

        assertEquals(28, entry.getDate().getDayOfMonth());
        assertEquals("org.caramba.CarambaContext", entry.getCaller());
        assertEquals(message, entry.getMessage());
        assertEquals(Level.WARN.toString(), entry.getLevel());
    }

    @Test
    public void test_category_and_line() throws Exception {
        LogEntry entry = singleEntry(" %c{1}:%L ", " AxisOperation:485 ");

        assertEquals("AxisOperation", entry.getCategory());
        assertEquals(485, entry.getLineNumber().intValue());
    }

    @Test
    public void test_parse_several() {
        EntryMatcher genreousMatcher = createGenerousMatcher();
        String textStart = "DEBUG_FRAME = org.apache.axis2.util.JavaUtils.callStackToString(JavaUtils.java:564)";
        String[] logs = {
                "02:00:33,543  DEBUG AxisOperation:485 - Exit: AxisOperation::setSoapAction",
                "02:00:33,544  DEBUG AxisService:815 - mapActionToOperation: Mapping Action to Operation: action: anonRobustOp; operation: org.apache.axis2.description.RobustOutOnlyAxisOperation@1a78071named: {http://ws.apache.org/namespaces/axis2}anonRobustOp",
                "02:00:33,584  DEBUG AxisService:821 - " + textStart,
                "    DEBUG_FRAME = org.apache.axis2.description.AxisService.mapActionToOperation(AxisService.java:821)",
                "    DEBUG_FRAME = org.apache.axis2.description.AxisService.addOperation(AxisService.java:667)",
                "    DEBUG_FRAME = org.apache.axis2.client.Stub.addAnonymousOperations(Stub.java:236)",
                "02:00:33,585  DEBUG AxisOperation:499 - Entry: AxisOperation::getInputAction",
                "02:00:33,586  DEBUG AxisOperation:504 - Debug: AxisOperation::getInputAction - using soapAction" };

        LogParser parser = new LogParser(A_WHILE_AGO, "%d{ABSOLUTE}  %5p %c{1}:%L - %m%n", genreousMatcher);
        for (String log : logs) {
            parser.addString(log);
        }
        List<LogEntry> entries = parser.getEntries();

        assertEquals(5, entries.size());
        String messageStr = entries.get(2).getMessage();
        assertEquals(textStart + NL + logs[3] + NL + logs[4] + NL + logs[5], messageStr);

    }

    private EntryMatcher createGenerousMatcher() {
        EntryMatcher genreousMatcher = mock(EntryMatcher.class);
        when(genreousMatcher.match((LogEntry) anyObject())).thenReturn(true);
        return genreousMatcher;
    }


}
