package org.xendan.logmonitor.read;

import org.apache.tools.ant.taskdefs.optional.ssh.SSHExec;
import org.xendan.logmonitor.HomeResolver;
import org.xendan.logmonitor.model.ServerSettings;

/**
 * User: kcyxa
 * Date: 1/26/13
 */
public class LogDownloader extends ScpSynchroniser {

    private static final String LAST_LOG = "last.log";

    public LogDownloader(ServerSettings settings) {
        super(settings);
    }

    public String downloadToLocal(String datePattern) {
        try {
            SSHExec exec = initTask(new SSHExec());
            exec.setCommand("set \"0,/" + datePattern + "/d\" <" + settings.getPath() + HomeResolver.HOME + "/" + LAST_LOG);
            exec.execute();
            return downloadFile(LAST_LOG, LAST_LOG);
        } catch (Exception e) {
            //TODO
            e.printStackTrace();
            return null;
        }
    }

}
