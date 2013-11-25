package org.xendan.logmonitor.read.command;

import com.intellij.idea.LoggerFactory;
import com.intellij.openapi.diagnostic.Logger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.optional.ssh.SSHExec;
import org.apache.tools.ant.taskdefs.optional.ssh.Scp;
import org.xendan.logmonitor.HomeResolver;
import org.xendan.logmonitor.model.Environment;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * User: kcyxa
 * Date: 1/26/13
 */
public class LogDownloader extends BaseCommand {

    private static final String FILTER_LAST_SH = "filter_last.sh";

    private static final String LAST_LOG = "last.log";
    private final String path;
    private final HomeResolver homeResolver;

    private static final Logger logger = LoggerFactory.getInstance().getLoggerInstance(LogDownloader.class.getCanonicalName());

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
        return "mkdir -p ~/" + HomeResolver.HOME + "\n" +
                getFilterLastCommand()
                        .replaceAll("\\$date", datePattern)
                        .replaceAll("\\$log_path", path)
                        .replaceAll("\\$download_path", "~/" + HomeResolver.HOME + "/" + LAST_LOG)
                        .replaceAll("\r\n", "\n")
                        .replaceAll("\r", "\n");
    }

    private String getFilterLastCommand() {
        File file = getCommandFile(homeResolver);
        if (file.exists()) {
            return readCommandFromFile(file);
        }
        return readCommandFromResource();
    }

    public static String readCommandFromResource() {
        InputStream resource = LogDownloader.class.getResourceAsStream("/" + FILTER_LAST_SH);
        try {
            return IOUtils.toString(resource);
        } catch (IOException e) {
             throw new IllegalStateException("Error reading command", e);
        } finally {
            try {
                if (resource != null) {
                    resource.close();
                }
            } catch (IOException e) {
                logger.error(e);
            }
        }
    }

    public static String readCommandFromFile(File file) {
        try {
            return FileUtils.readFileToString(file);
        } catch (IOException e) {
            throw new IllegalStateException("Error reading command", e);
        }
    }

    public static File getCommandFile(HomeResolver homeResolver) {
        return new File(homeResolver.getPath(FILTER_LAST_SH));
    }
}
