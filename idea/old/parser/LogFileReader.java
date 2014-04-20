package org.xendan.logmonitor.parser;

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
public class LogFileReader {
    private final String logFile;
    private final String logPattern;
    private final Environment environment;

    public LogFileReader(String logFile, String logPattern, Environment environment) {
        this.logFile = logFile;
        this.logPattern = logPattern;
        this.environment = environment;
    }

    public List<LogEntry> getEntries() {
        LogParser parser = new LogParser(logPattern, environment);
        readEntries(logFile, parser);
        environment.setLastUpdate(parser.getLastTime());
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
