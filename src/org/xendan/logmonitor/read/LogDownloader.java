package org.xendan.logmonitor.read;

import org.apache.tools.ant.taskdefs.optional.ssh.SSHExec;
import org.apache.tools.ant.taskdefs.optional.ssh.Scp;
import org.xendan.logmonitor.HomeResolver;
import org.xendan.logmonitor.model.LogSettings;

/**
 * User: kcyxa
 * Date: 1/26/13
 */
public class LogDownloader extends ScpSynchroniser {

    private static final String LAST_LOG = "last.log";
    private final String path;

    public LogDownloader(LogSettings settings) {
        super(settings.getServer());
        path = settings.getPath();
    }

    public String downloadToLocal(String datePattern) {
        try {
            if (datePattern == null) {
                Scp scp = initTask(new Scp());
                scp.setFile(getServerRoot() + path);
                return downloadTo(LAST_LOG, scp);
            }
            SSHExec exec = initTask(new SSHExec());
            exec.setCommand("mkdir -p ~/" + HomeResolver.HOME + "; sed \"0,/" + datePattern + "/d\" <" + path + " > ~/" + HomeResolver.HOME + "/" + LAST_LOG);
            exec.execute();
            return downloadFile(LAST_LOG, LAST_LOG);
        } catch (Exception e) {
            //TODO
            e.printStackTrace();
            return null;
        }
    }

}
