package org.xendan.logmonitor.parser;
import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.xendan.logmonitor.model.Level;
import org.xendan.logmonitor.model.LogEntry;

public class LogParserTest {
    
    public static final String PATTERN = "%d{yyyy-MM-dd HH:mm:ss,SSS} %-5p [%C] %m%n";

    private static final String MESSAGE = "some message not multiline";
    
    public static final String LOG = "2012-09-28 01:12:17,191 WARN  [org.caramba.CarambaContext] " + MESSAGE;
    

    @Test
    public void test_parse_date()  {
        LogParser parser = new LogParser("%d{yyyy-MM-dd HH:mm:ss,SSS}");
        LogEntry entry = parser.parse("2012-09-28 01:12:17,191");
        
        assertEquals(28, entry.getDate().getDayOfMonth());
    }
    
    @Test
    public void test_parse_level() throws Exception {
        LogParser parser = new LogParser("%-5p");
        LogEntry entry = parser.parse("WARN ");
        
        assertEquals(Level.WARN, entry.getLevel());
    }
    
    @Test
    public void test_parse_caller() throws Exception {
        LogParser parser = new LogParser("%C");
        LogEntry entry = parser.parse("org.caramba.CarambaContext");
        
        assertEquals("org.caramba.CarambaContext", entry.getCaller());
    }
    
    @Test
    public void test_parse_message() throws Exception {
        LogParser parser = new LogParser("%m");
        LogEntry entry = parser.parse("hello world");
        
        assertEquals("hello world", entry.getMessage());
    }
    
    @Test
    public void test_parse_date_level() throws Exception {
        LogParser parser = new LogParser("%d{yyyy-MM-dd HH:mm:ss,SSS} %-5p");
        LogEntry entry = parser.parse("2012-09-28 01:12:17,191 WARN ");
        
        assertEquals(28, entry.getDate().getDayOfMonth());
        assertEquals(Level.WARN, entry.getLevel());
    }
    
    @Test
    public void test_parse_all_no_message() throws Exception {
        LogParser parser = new LogParser("%d{yyyy-MM-dd HH:mm:ss,SSS} %-5p [%C]");
        LogEntry entry = parser.parse("2012-09-28 01:12:17,191 WARN  [org.caramba.CarambaContext]");
        
        assertEquals(28, entry.getDate().getDayOfMonth());
        assertEquals("org.caramba.CarambaContext", entry.getCaller());
        assertEquals(Level.WARN, entry.getLevel());
    }
    
    @Test
    public void test_parse_all() throws Exception {
        LogParser parser = new LogParser(PATTERN);
        LogEntry entry = parser.parse(LOG);
        
        assertEquals(28, entry.getDate().getDayOfMonth());
        assertEquals("org.caramba.CarambaContext", entry.getCaller());
        assertEquals(MESSAGE, entry.getMessage());
        assertEquals(Level.WARN, entry.getLevel());
    }
}
