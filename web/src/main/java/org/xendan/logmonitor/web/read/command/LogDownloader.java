package org.xendan.logmonitor.web.read.command;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.optional.ssh.SSHExec;
import org.apache.tools.ant.taskdefs.optional.ssh.Scp;
import org.xendan.logmonitor.HomeResolver;
import org.xendan.logmonitor.model.Environment;
import org.xendan.logmonitor.web.service.EnvironmentMessage;
import org.xendan.logmonitor.web.service.EnvironmentMonitor;
import org.xendan.logmonitor.web.service.LogService;

/**
 * User: kcyxa
 * Date: 1/26/13
 */
public class LogDownloader extends BaseCommand {

    private static final Logger logger = Logger.getLogger(LogDownloader.class);

    protected static final String LAST_LOG = "last.log";
    public static final String SIZE_PROPERTY = "size";
    protected final Environment environment;
    private final HomeResolver homeResolver;
    private final CommandFileLoader fileLoader;
    private final String project;
    private final EnvironmentMonitor monitor;

    public LogDownloader(Environment settings, HomeResolver homeResolver, String project, LogService service) {
        this(settings, homeResolver, CommandFileLoader.createFilter(homeResolver) ,project, service);
    }

    public LogDownloader(Environment environment, HomeResolver homeResolver, CommandFileLoader fileLoader, String project, EnvironmentMonitor monitor) {
        super(environment.getServer());
        this.homeResolver = homeResolver;
        this.fileLoader = fileLoader;
        this.project = project;
        this.monitor = monitor;
        this.environment = environment;
    }

    private String downloadTo(String localPath, Scp scp) {
        scp.setLocalTofile(localPath);
        try {
            scp.execute();
        } catch (BuildException e) {
            if (e.getMessage().contains("No such file or directory")) {
                monitor.setEnvironmentStatus(environment, EnvironmentMessage.FILE_NOT_FOUND);
                return null;
            }
            monitor.setErrorDownloading(environment, e);
            logger.error("Error file download", e);
            return null;
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
            return environment.getPath();
        }
        String localPath = homeResolver.joinMkDirs(LAST_LOG, project, environment.getName());
        monitor.setDownloadStartedTo(environment, localPath);
        SSHExec exec = initTask(new SSHExec());
        String command = buildCommand(datePattern);
        exec.setCommand(command);
        exec.setOutputproperty(SIZE_PROPERTY);
        logger.info("Run on " + server + " :\n" + command);
        try {
            exec.execute();
            String size = exec.getProject().getProperty(SIZE_PROPERTY);
            if (size != null) {
                size = size.replaceAll("\\s+", "");
            }
            monitor.setFileSizeCalculated(environment, size);
        } catch (BuildException e) {
           monitor.setErrorSizeCalculation(environment, e);
           logger.error("Error calculating file size", e);
        }

        Scp scp = initTask(new Scp());
        scp.setFile(getServerDir() + "/" + LAST_LOG);
        return downloadTo(localPath, scp);
    }

    protected String buildCommand(String datePattern) {
        return "mkdir -p ~/" + HomeResolver.HOME + "\n" +  replaceParameters(StringUtils.defaultString(datePattern), fileLoader.getShCommand());
    }

    protected String replaceParameters(String datePattern, String shCommand) {
        return shCommand.replaceAll("\\$date", datePattern)
                .replaceAll("\\$log_path", environment.getPath())
                .replaceAll("\\$download_path", "~/" + HomeResolver.HOME + "/" + LAST_LOG)
                .replaceAll("\r\n", "\n")
                .replaceAll("\r", "\n");
    }

}
