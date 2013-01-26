package org.xendan.logmonitor;

import java.util.List;

import org.joda.time.DateTime;
import org.xendan.logmonitor.model.LogEntry;

public class RemoteLogReader extends LocalLogReader {

    private final String host;
    private final String login;
    private final String password;
    private final String remotePath;

    public RemoteLogReader(String pattern, String host, String login, String password, String remotePath) {
        super(pattern, buildPath());
        this.host = host;
        this.login = login;
        this.password = password;
        this.remotePath = remotePath;
    }

    private static String buildPath() {
        return System.getProperty("user.home") + "/.logmonitor/tmp.log";
    }

    @Override
    public List<LogEntry> readSince(DateTime lastDate) {
        copyToLocalTemp();
        return super.readSince(lastDate);
    }

    private void copyToLocalTemp() {
        // TODO Auto-generated method stub
        
    }
}
