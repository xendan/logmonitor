package org.xendan.logmonitor.parser;

import org.xendan.logmonitor.model.LogEntry;
import org.xendan.logmonitor.model.Matchers;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

/**
 * User: id967161
 * Date: 09/09/13
 */
public class LogFileParser {
    private final String logFile;
    private final String logPattern;
    private final Matchers matchers;

    public LogFileParser(String logFile, String logPattern, Matchers matchers) {
        this.logFile = logFile;
        this.logPattern = logPattern;
        this.matchers = matchers;
    }

    public List<LogEntry> getEntries() {
        LogParser parser = new LogParser(logPattern, matchers);
        readEntries(logFile, parser);
        return parser.getEntries();
    }

    private static void readEntries(String path, LogParser parser) {
        FileReader fileReader = null;
        BufferedReader br = null;
        try {
            fileReader = new FileReader(path);
            br = new BufferedReader(fileReader);
            String line;
            while ((line = br.readLine()) != null) {
                parser.addString(line);
            }
            br.close();
        } catch (IOException e) {
            throw new IllegalArgumentException("Error reading log file", e);
        } finally {
            try {
                if (fileReader != null) {
                    fileReader.close();
                }
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                //TODO: log
            }
        }
    }
}
