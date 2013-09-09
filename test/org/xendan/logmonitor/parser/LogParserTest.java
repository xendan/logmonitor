package org.xendan.logmonitor.parser;

import org.apache.log4j.Level;
import org.junit.Test;
import org.xendan.logmonitor.model.LogEntry;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
public class LogParserTest {

//    public static final String PATTERN = "%d{yyyy-MM-dd HH:mm:ss,SSS} %-5p [%C] %m%n";

    private static final String MESSAGE = "some message not multiline";

//    private static final long WARN_READ = DateTimeFormat.forPattern(DateParser.DEFAULT_FORMAT).parseDateTime("2012-09-21 01:12:17,191").getMillis();

    public static final String LOG_WARN = "2012-09-21 01:12:17,191 WARN  [org.caramba.CarambaContext] " + MESSAGE;
    public static final String LOG_INFO = "2012-09-22 01:12:17,191 INFO  [org.caramba.CarambaContext] " + MESSAGE;
    public static final String LOG_ERROR = "2012-09-23 01:12:17,191 ERROR [org.caramba.CarambaContext] " + MESSAGE;
    public static final String LOG_DEBUG = "2012-09-24 01:12:17,191 DEBUG [org.caramba.CarambaContext] " + MESSAGE;
    private static final String NL = System.getProperty("line.separator");
    public static final String FULL_PATTERN = "%d{yyyy-MM-dd HH:mm:ss,SSS} %-5p [%C]";
    private static final EntryMatcher INFO_MATCHER = EntryMatcherTest.createInfoMatchers();

    @Test
    public void test_parse_date() {
        LogEntry entry = singleEntry("%d{yyyy-MM-dd HH:mm:ss,SSS}", "2012-09-28 01:12:17,191");
        assertEquals(28, entry.getDate().getDayOfMonth());
    }

    private LogEntry singleEntry(String pattern, String log) {
        EntryMatcher genreousMatcher = createGenerousMatcher();
        LogParser parser = new LogParser(pattern, genreousMatcher);
        parser.addString(log);
        return parser.getEntries().get(0);
    }


    @Test
    public void test_parse_level() throws Exception {
        LogEntry entry = singleEntry("%-5p", "WARN ");
        assertEquals(Level.WARN.toString(), entry.getLevel());
    }
    
    @Test
    public void test_clear() throws Exception {
        LogParser parser = new LogParser("%-5p", INFO_MATCHER);
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
        LogParser parser = new LogParser(FULL_PATTERN, INFO_MATCHER);
        parser.addString(LOG_WARN);
        parser.addString(LOG_ERROR);
        parser.addString(LOG_INFO);
        parser.addString(LOG_DEBUG);

        assertEquals("Expect only warn read", 3, parser.getEntries().size());
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
 
        LogParser parser = new LogParser("%d{ABSOLUTE}  %5p %c{1}:%L - %m%n", genreousMatcher);
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
