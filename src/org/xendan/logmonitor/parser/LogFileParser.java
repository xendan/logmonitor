package org.xendan.logmonitor.parser;

import org.joda.time.LocalDateTime;
import org.xendan.logmonitor.model.Environment;
import org.xendan.logmonitor.model.LogEntry;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

/**
 * User: id967161
 * Date: 09/09/13
 */
public class LogFileParser {
    private final LocalDateTime since;
    private final String logFile;
    private final String logPattern;
    private final Environment environment;

    public LogFileParser(LocalDateTime since, String logFile, String logPattern, Environment environment) {
        this.since = since;
        this.logFile = logFile;
        this.logPattern = logPattern;

        this.environment = environment;
    }

    public List<LogEntry> getEntries() {
        LogParser parser = new LogParser(since, logPattern, environment);
        readEntries(logFile, parser);
        return parser.getEntries();
    }

    private void readEntries(String path, LogParser parser) {
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
