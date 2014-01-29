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

    protected static final String LAST_LOG = "last.log";
    protected final String path;
    private final HomeResolver homeResolver;
    private final CommandFileLoader fileLoader;

    private final String[] dirs;

    public LogDownloader(Environment settings, HomeResolver homeResolver, String project) {
        this(settings, homeResolver, CommandFileLoader.createFilter(homeResolver) ,project);
    }

    public LogDownloader(Environment environment, HomeResolver homeResolver, CommandFileLoader fileLoader, String project) {
        super(environment.getServer());
        this.homeResolver = homeResolver;
        this.fileLoader = fileLoader;
        path = environment.getPath();
        dirs = new String[]{project, environment.getName()};
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
        return server.getLogin() + ":" + server.getPassword() + "@" + server.getHost() + ":";
    }

    public String downloadToLocal(String datePattern) {
        if (server == null) {
            return path;
        }
        String localPath = homeResolver.joinMkDirs(LAST_LOG, dirs);
        if (datePattern == null) {
            Scp scp = initTask(new Scp());
            scp.setFile(getServerRoot() + path);
            return downloadTo(localPath, scp);
        }
        SSHExec exec = initTask(new SSHExec());
        String command = buildCommand(datePattern);
        exec.setCommand(command);
        System.out.println(command);
        exec.execute();
        Scp scp = initTask(new Scp());
        scp.setFile(getServerDir() + "/" + LAST_LOG);
        return downloadTo(localPath, scp);
    }

    protected String buildCommand(String datePattern) {
        return "mkdir -p ~/" + HomeResolver.HOME + "\n" +  replaceParameters(datePattern, fileLoader.getShCommand());
    }

    protected String replaceParameters(String datePattern, String shCommand) {
        return shCommand.replaceAll("\\$date", datePattern)
                .replaceAll("\\$log_path", path)
                .replaceAll("\\$download_path", "~/" + HomeResolver.HOME + "/" + LAST_LOG)
                .replaceAll("\r\n", "\n")
                .replaceAll("\r", "\n");
    }

}
