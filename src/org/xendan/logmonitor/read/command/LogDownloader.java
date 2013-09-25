package org.xendan.logmonitor.read.command;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.optional.ssh.SSHExec;
import org.apache.tools.ant.taskdefs.optional.ssh.Scp;
import org.xendan.logmonitor.HomeResolver;
import org.xendan.logmonitor.model.Environment;

/**
 * User: kcyxa
 * Date: 1/26/13
 */
public class LogDownloader extends BaseCommand {

    private static final String LAST_LOG = "last.log";
    private final String path;
    private final HomeResolver homeResolver;

    public LogDownloader(Environment settings, HomeResolver homeResolver) {
        super(settings.getServer());
        this.homeResolver = homeResolver;
        path = settings.getPath();
    }

    private String downloadTo(String localPath, Scp scp) {
        scp.setLocalTofile(localPath);
        try {
            scp.execute();
        } catch (BuildException e) {
            if (e.getMessage().contains("No such file or directory")) {
                return null;
            }
            throw new IllegalStateException(e);
        }
        return localPath;
    }

    private String getServerDir() {
        return getServerRoot() + "~/" + HomeResolver.HOME;
    }

    private String getServerRoot() {
        return settings.getLogin() + ":" + settings.getPassword() + "@" + settings.getHost() + ":";
    }

    public String downloadToLocal(String datePattern, String... dirs) {
        String localPath = homeResolver.joinMkDirs(LAST_LOG, dirs);
        if (datePattern == null) {
            Scp scp = initTask(new Scp());
            scp.setFile(getServerRoot() + path);
            return downloadTo(localPath, scp);
        }
        SSHExec exec = initTask(new SSHExec());
        exec.setCommand(buildCommand(datePattern));
        exec.execute();
        Scp scp = initTask(new Scp());
        scp.setFile(getServerDir() + "/" + LAST_LOG);
        return downloadTo(localPath, scp);
    }

    private String buildCommand(String datePattern) {
        String serverPath = "~/" + HomeResolver.HOME + "/" + LAST_LOG;
        String mkdir = "mkdir -p ~/" + HomeResolver.HOME;
        String ifContains = "if grep -q '" + datePattern + "' " + path + "; then \n";
        String sed = "sed \"0,/" + datePattern + "/d\" <" + path + " > " + serverPath;
        return mkdir + "\n" + ifContains + sed + "\n else cp " + path + " " + serverPath + "\n fi";
    }

}
