package org.xendan.logmonitor.read;

import org.apache.log4j.Logger;
import org.xendan.logmonitor.model.Environment;
import org.xendan.logmonitor.parser.EntryStatusListener;
import org.xendan.logmonitor.read.command.FileLoadState;

import java.io.File;
import java.util.Map;

/**
 * User: id967161
 * Date: 20/03/14
 */
public class EnvironmentStateMonitor implements Runnable {
    private static final Logger logger = Logger.getLogger(EnvironmentStateMonitor.class);

    private final Map<Environment, FileLoadState> loadingStates;
    private final EntryStatusListener listener;

    public EnvironmentStateMonitor(Map<Environment, FileLoadState> loadingStates, EntryStatusListener listener) {
        this.loadingStates = loadingStates;
        this.listener = listener;
    }

    @Override
    public void run() {
        for (Map.Entry<Environment, FileLoadState> entry : loadingStates.entrySet()) {
          switch (entry.getValue().getState()) {
              case WAITING_DOWNLOAD_START:
                  showPrepareDownload(entry.getKey());
                  break;
              case WAITING_DOWNLOAD:
                  showDownloadPercentage(entry.getKey(), entry.getValue().getLocalPath(), entry.getValue().getFileSize());
                  break;
              case WAITING_PARSE:
                  showWaitingParse(entry.getKey());
                  break;
              case WAITING_UPDATE:
                  showIsIdle(entry.getKey(), entry.getValue().isEntriesNotFound(), entry.getValue().getErrorMessage(), entry.getValue().getException());
                  break;
          }
        }
    }

    private void showWaitingParse(Environment environment) {
        showInfo(environment, "log downloaded, waiting parse");
    }

    private void showInfo(Environment environment, String message) {
        listener.setEnvironmentMessage(environment, message, false);
    }

    private void showPrepareDownload(Environment environment) {
        showInfo(environment, "download started");
    }

    private void showDownloadPercentage(Environment environment, String localPath, long fileSize) {
         showInfo(environment, " log downloaded " + downloaded(localPath, fileSize) + " %");
    }

    private String downloaded(String localPath, long fileSize) {
        if (fileSize <= 0) {
            return  "unknown";
        }
        File file = new File(localPath);
        if (!file.exists()) {
            return "0";
        }
        return String.valueOf(Math.round(((double) file.length()) / fileSize * 100));
    }

    private void showIsIdle(Environment environment, boolean entriesNotFound, String errorMessage, Throwable exception) {
        if (entriesNotFound) {
            listener.onEntriesNotFound(environment);
        } else if (errorMessage != null) {
            logger.error(errorMessage, exception);
            listener.setEnvironmentMessage(environment, errorMessage + "\n" + exception.getMessage(), true);
        } else {
            showInfo(environment, "Next update: " + environment.getNextUpdate());
        }
    }
}
