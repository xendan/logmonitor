package org.xendan.logmonitor.read;

import com.intellij.openapi.components.ServiceManager;
import org.apache.commons.io.IOUtils;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.optional.ssh.SSHBase;
import org.apache.tools.ant.taskdefs.optional.ssh.SSHExec;
import org.apache.tools.ant.taskdefs.optional.ssh.Scp;
import org.xendan.logmonitor.HomeResolver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * User: kcyxa
 * Date: 1/26/13
 */
public class ScpDownloader {

    private static final String GREP_ERRORES_PY = "grep_errores.py";
    private static final String LOGMONITOR = ".logmonitor";
    private static final String LAST_LOG = "last.log";
    private String host;
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
        try {
            SSHExec exec = initTask(new SSHExec());
            exec.setCommand("mkdir -p ~/" + LOGMONITOR);
            exec.execute();
            Scp scp = initTask(new Scp());
            File file = copyFile();
            scp.setFile(file.getAbsolutePath());
            scp.setTodir(getToDir());
            scp.execute();
            exec = initTask(new SSHExec());
            exec.setCommand("python ~/" + LOGMONITOR + "/" + GREP_ERRORES_PY + " " + path + " 'ERROR'");
            exec.execute();
            //POINT: use idea system
            String localPath = ServiceManager.getService(HomeResolver.class).getPath(LAST_LOG);
            scp = initTask(new Scp());
            scp.setFile(getToDir() + "/" + LAST_LOG);
            scp.setLocalTofile(localPath);
            scp.execute();
            return localPath;
        } catch (Exception e) {
            //TODO
            e.printStackTrace();
            return null;
        }
    }

    private String getToDir() {
        return user + ":" + password + "@" + host + ":~/" + LOGMONITOR;
    }

    private File copyFile() throws IOException {
        File file = new File(ServiceManager.getService(HomeResolver.class).getPath(GREP_ERRORES_PY));
        if (!file.exists()) {
            if (!file.createNewFile()) {
                throw new IllegalStateException("Error creating file" + file.getAbsolutePath());
            }
            IOUtils.copy(getClass().getResourceAsStream("/" + GREP_ERRORES_PY), new FileOutputStream(file, false));

        }
        return file;
    }

    private <T extends SSHBase> T initTask(T task) {
        task.setProject(new Project()); // prevent a NPE (Ant works with projects)
        task.setTrust(true); // workaround for not supplying known hosts file
        task.setPassword(password);
        task.setHost(host);
        task.setUsername(user);
        return task;
    }
}
