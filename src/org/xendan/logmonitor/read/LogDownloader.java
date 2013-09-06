package org.xendan.logmonitor.read;

import com.intellij.openapi.components.ServiceManager;
import org.apache.commons.io.IOUtils;
import org.apache.tools.ant.taskdefs.optional.ssh.SSHExec;
import org.xendan.logmonitor.HomeResolver;
import org.xendan.logmonitor.model.ServerSettings;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * User: kcyxa
 * Date: 1/26/13
 */
public class LogDownloader extends ScpSynchroniser {

    private static final String GREP_ERRORES_PY = "grep_errores.py";
    private static final String LAST_LOG = "last.log";

    public LogDownloader(ServerSettings settings) {
        super(settings);
    }

    public String downloadToLocal() {
        try {
            uploadFile("", copyGrepErrorsFile());
            SSHExec exec = initTask(new SSHExec());
            exec.setCommand("python ~/" + HomeResolver.HOME + "/" + GREP_ERRORES_PY + " " + settings.getPath() + " 'ERROR'");
            exec.execute();
            //POINT: use idea system
            return downloadFile(LAST_LOG, LAST_LOG);
        } catch (Exception e) {
            //TODO
            e.printStackTrace();
            return null;
        }
    }

    private String copyGrepErrorsFile() throws IOException {
        File file = new File(ServiceManager.getService(HomeResolver.class).getPath(GREP_ERRORES_PY));
        if (!file.exists()) {
            if (!file.createNewFile()) {
                throw new IllegalStateException("Error creating file" + file.getAbsolutePath());
            }
            IOUtils.copy(getClass().getResourceAsStream("/" + GREP_ERRORES_PY), new FileOutputStream(file, false));

        }
        return GREP_ERRORES_PY;
    }
}
