package org.xendan.logmonitor;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.joda.time.DateTime;
import org.xendan.logmonitor.model.LogEntry;
import org.xendan.logmonitor.parser.LogParser;

public class LocalLogReader {
    
    private final String path;
    private final LogParser parser;
    private final String pattern;

    public LocalLogReader(String pattern, String path) {
        this.pattern = pattern;
        this.parser = new LogParser(pattern);
        this.path = path;
    }

    public List<LogEntry> readSince(DateTime lastDate) {
        BufferedReader br = getBufferedReader();
        String line;
        boolean firstRead = true;
        try {
            while((line = br.readLine()) != null) {
                LogEntry entry = parser.addString(line);
                if (firstRead && entry == null) {
                    throw new IllegalArgumentException("log line " + line + " doesn't mathc pattern " + pattern);
                }
                firstRead = false;
                if (isOutdated(lastDate, entry)) {
                    parser.clear();
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Error reading log file " + path, e);
        }
        return parser.getEntries();
    }

    private boolean isOutdated(DateTime lastDate, LogEntry entry) {
        return entry != null && (entry.getDate().isBefore(lastDate) || entry.getDate().equals(lastDate));
    }

    private BufferedReader getBufferedReader() {
        try {
            return new BufferedReader(new FileReader(path));
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("File not found" + path, e);
        }
    }

}
