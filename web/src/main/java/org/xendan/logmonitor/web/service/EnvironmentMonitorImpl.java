package org.xendan.logmonitor.web.service;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.apache.tools.ant.BuildException;
import org.xendan.logmonitor.model.Environment;
import org.xendan.logmonitor.model.EnvironmentStatus;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class EnvironmentMonitorImpl implements EnvironmentMonitor {
    private static final Logger logger = Logger.getLogger(EnvironmentMonitorImpl.class);

    private final Map<Environment, EnvironmentInfo> infos = new HashMap<Environment, EnvironmentInfo>();

    @Override
    public void setEnvironmentStatus(Environment environment, EnvironmentMessage message) {
        EnvironmentInfo info = getOrCreateInfo(environment);
        info.setMessage(message);
        info.setError(false);
    }

    private EnvironmentInfo getOrCreateInfo(Environment environment) {
        if (!infos.containsKey(environment)) {
            infos.put(environment, new EnvironmentInfo());
        }
        return infos.get(environment);
    }

    @Override
    public void setErrorDownloading(Environment environment, BuildException e) {
        setErrorStatus(environment, EnvironmentMessage.ERROR_DOWNLOADING, e);
    }

    private void setErrorStatus(Environment environment, EnvironmentMessage message, Exception exception) {
        EnvironmentInfo info = getOrCreateInfo(environment);
        info.setMessage(message);
        info.setException(exception);
    }

    @Override
    public void setDownloadStartedTo(Environment environment, String localPath) {
        EnvironmentInfo info = getOrCreateInfo(environment);
        info.setFilePath(localPath);
        info.setMessage(EnvironmentMessage.DOWNLOADING);
        info.setError(false);
    }

    private String downloaded(String localPath, long fileSize) {
        if (fileSize <= 0) {
            return  "unknown";
        }
        File file = new File(localPath);
        if (!file.exists()) {
            return "0% of " + fileSize;
        }
        return String.valueOf(Math.round(((double) file.length()) / fileSize * 100)) + "% of " + fileSize ;
    }

    @Override
    public void setFileSizeCalculated(Environment environment, String sizeStr) {
        EnvironmentInfo info = getOrCreateInfo(environment);
        info.setError(false);
        try {
            info.setFileSize(Long.valueOf(sizeStr));
        } catch (NumberFormatException e) {
            logger.error("Error getting file size", e);
            setErrorSizeCalculation(environment, e);
        }

    }

    @Override
    public void setErrorSizeCalculation(Environment environment, Exception e) {
        EnvironmentInfo info = getOrCreateInfo(environment);
        info.setMessage(EnvironmentMessage.ERROR_GETTING_FILE_SIZE);
        info.setException(e);
    }

    @Override
    public void clear() {
        infos.clear();
    }

    @Override
    public EnvironmentStatus getStatus(Long envId) {
        EnvironmentInfo info = getById(envId);
        if (info == null) {
            return createOkStatus(EnvironmentMessage.WAITING);
        }
        if (info.isError()) {
            return createErrorStatus(info.getMessage(), info.getException());
        }
        if (info.getMessage() == EnvironmentMessage.DOWNLOADING) {
            return createDownloadingMessage(info);
        }
        return createOkStatus(info.getMessage());
    }

    private EnvironmentStatus createDownloadingMessage(EnvironmentInfo info) {
        return new EnvironmentStatus(EnvironmentMessage.DOWNLOADING + " " + downloaded(info.getFilePath(), info.getFileSize()), false);
    }

    private EnvironmentStatus createErrorStatus(EnvironmentMessage message, Exception exception) {
        return new EnvironmentStatus(message.getText(), ExceptionUtils.getStackTrace(exception));
    }

    private EnvironmentStatus createOkStatus(EnvironmentMessage message) {
        return new EnvironmentStatus(message.getText(), false);
    }

    private EnvironmentInfo getById(Long envId) {
        for (Map.Entry<Environment,EnvironmentInfo> entry : infos.entrySet()) {
            if (entry.getKey().getId().equals(envId)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private class EnvironmentInfo {
        private boolean error;
        private Exception exception;
        private EnvironmentMessage message;
        private String filePath;
        private long fileSize;

        public boolean isError() {
            return error;
        }

        public void setError(boolean error) {
            if (!error) {
                exception = null;
            }
            this.error = error;
        }

        public Exception getException() {
            return exception;
        }

        public void setException(Exception exception) {
            this.error = true;
            this.exception = exception;
        }

        public EnvironmentMessage getMessage() {
            return message;
        }

        public void setMessage(EnvironmentMessage message) {
            this.message = message;
        }

        public String getFilePath() {
            return filePath;
        }

        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }

        public long getFileSize() {
            return fileSize;
        }

        public void setFileSize(long fileSize) {
            this.fileSize = fileSize;
        }
    }
}
