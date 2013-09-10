package org.xendan.logmonitor.read;

import com.intellij.openapi.components.ServiceManager;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.optional.ssh.SSHBase;
import org.apache.tools.ant.taskdefs.optional.ssh.SSHExec;
import org.apache.tools.ant.taskdefs.optional.ssh.Scp;
import org.xendan.logmonitor.HomeResolver;
import org.xendan.logmonitor.model.ServerSettings;

/**
 * User: kcyxa
 * Date: 1/26/13
 */
public class ScpSynchroniser {

    protected final ServerSettings settings;
    private final HomeResolver homeResolver;

    public ScpSynchroniser(ServerSettings settings, HomeResolver homeResolver) {
        this.settings = settings;
        this.homeResolver = homeResolver;
    }
    public ScpSynchroniser(ServerSettings settings) {
        this(settings, ServiceManager.getService(HomeResolver.class));
    }


    public void uploadFile(String serverDirPath, String localPath) {
        SSHExec exec = initTask(new SSHExec());
        exec.setCommand("mkdir -p ~/" + HomeResolver.HOME + serverDirPath);
        exec.execute();
        Scp scp = initTask(new Scp());
        scp.setFile(homeResolver.getPath(localPath));
        scp.setTodir(getServerDir() + serverDirPath);
        scp.execute();
    }

    public String downloadFile(String remotePath, String localPath) {
        localPath = homeResolver.getPath(localPath);
        Scp scp = initTask(new Scp());
        scp.setFile(getServerDir() + "/" + remotePath);
        return downloadTo(localPath, scp);
    }

    protected String downloadTo(String localPath, Scp scp) {
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

    protected String getServerDir() {
        return getServerRoot() + "~/" + HomeResolver.HOME;
    }

    protected String getServerRoot() {
        return settings.getLogin() + ":" + settings.getPassword() + "@" + settings.getHost() + ":";
    }

    protected <T extends SSHBase> T initTask(T task) {
        task.setProject(new Project()); // prevent a NPE (Ant works with projects)
        task.setTrust(true); // workaround for not supplying known hosts file
        task.setPassword(settings.getPassword());
        task.setHost(settings.getHost());
        task.setUsername(settings.getLogin());
        return task;
    }

    public String getSeverName() {
        return settings.getName();
    }
}
