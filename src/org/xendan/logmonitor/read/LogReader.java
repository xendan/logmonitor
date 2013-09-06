package org.xendan.logmonitor.read;

import org.joda.time.DateTime;
import org.xendan.logmonitor.model.LogEntry;
import org.xendan.logmonitor.parser.LogParser;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class LogReader {
    private final LogParser parser;
    private final String pattern;
    private LogDownloader logDownloader;


    public LogReader(String pattern, LogDownloader logDownloader) {
        this.pattern = pattern;
        this.logDownloader = logDownloader;
        this.parser = new LogParser(pattern);
    }

    public List<LogEntry> readSince(DateTime lastDate) {
        String path = logDownloader.downloadToLocal();
        BufferedReader bufferedReader = getBufferedReader(path);
        String line;
        boolean firstRead = true;
        try {
            while((line = bufferedReader.readLine()) != null) {
                LogEntry entry = parser.addString(line);
                if (firstRead && entry == null) {
                    throw new IllegalArgumentException("log line " + line + " doesn't match pattern " + pattern);
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

    private BufferedReader getBufferedReader(String path) {
        try {
            return new BufferedReader(new FileReader(path));
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("File not found" + path, e);
        }
    }

}
