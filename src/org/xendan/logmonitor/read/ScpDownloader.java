package org.xendan.logmonitor.read;

import com.jcraft.jsch.UserInfo;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.optional.ssh.Scp;

/**
 * User: kcyxa
 * Date: 1/26/13
 */
public class ScpDownloader {

    private String host;
    private UserInfo info;
    private final String user;
    private final String password;
    private String path;

    public ScpDownloader(String host, String user, String password, String path) {
        this.host = host;
        this.user = user;
        this.password = password;
        this.path = path;
    }

    public String downloadToLocal() {
        //POINT: use idea system
        String localpath  = System.getProperty("user.home") + "/.logmonitor/last.log";
        Scp scp = new Scp();
        scp.setFile(user + ":" + password + "@" + host + ":" + path);
        scp.setLocalTofile(localpath);
        scp.setProject(new Project()); // prevent a NPE (Ant works with projects)
        scp.setTrust(true); // workaround for not supplying known hosts file
        scp.execute();
        return localpath;
    }
}
