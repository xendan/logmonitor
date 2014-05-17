package org.xendan.logmonitor.web.read.command;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.xendan.logmonitor.HomeResolver;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * User: id967161
 * Date: 05/12/13
 */
public class CommandFileLoader {
//    private static final Logger log = LoggerFactory.getLoggerInstance(AdvancedSettings.class.getCanonicalName());
    private static final Logger log = Logger.getLogger(CommandFileLoader.class);

    private static final String FILTER_LAST_SH = "filter_last.sh";
    private static final String ROUND_LOG_SH = "round_log.sh";

    private final HomeResolver homeResolver;
    private final String command;

    public static CommandFileLoader createFilter(HomeResolver homeResolver) {
        return new CommandFileLoader(homeResolver, FILTER_LAST_SH);
    }

    public static CommandFileLoader createRound(HomeResolver homeResolver) {
        return new CommandFileLoader(homeResolver, ROUND_LOG_SH);
    }

    private CommandFileLoader(HomeResolver homeResolver, String command) {
        this.homeResolver = homeResolver;
        this.command = command;
    }


    public File getCommandFile() {
        return new File(homeResolver.getPath(command));
    }


    public String readCommandFromResource() {
        InputStream resource = LogDownloader.class.getResourceAsStream("/" + command);
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
                log.error(e);
            }
        }
    }

    public String readCommandFromFile(File file) {
        try {
            return FileUtils.readFileToString(file);
        } catch (IOException e) {
            throw new IllegalStateException("Error reading command", e);
        }
    }

    public String getShCommand() {
        File file = getCommandFile();
        if (file.exists()) {
            return readCommandFromFile(file);
        }
        return readCommandFromResource();
    }
}
