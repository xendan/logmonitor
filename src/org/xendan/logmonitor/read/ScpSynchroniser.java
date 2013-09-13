package org.xendan.logmonitor.read;

import com.intellij.openapi.components.ServiceManager;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.optional.ssh.Scp;
import org.xendan.logmonitor.HomeResolver;
import org.xendan.logmonitor.model.Server;

/**
 * User: kcyxa
 * Date: 1/26/13
 */
public class ScpSynchroniser extends BaseCommand {

    private final HomeResolver homeResolver;

    public ScpSynchroniser(Server settings, HomeResolver homeResolver) {
        super(settings);
        this.homeResolver = homeResolver;
    }
    public ScpSynchroniser(Server settings) {
        this(settings, ServiceManager.getService(HomeResolver.class));
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

}
