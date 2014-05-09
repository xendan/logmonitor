package org.xendan.logmonitor.web.service;

import org.apache.tools.ant.BuildException;
import org.xendan.logmonitor.model.Environment;
import org.xendan.logmonitor.model.EnvironmentStatus;

public interface EnvironmentMonitor {
    void setEnvironmentStatus(Environment environment, EnvironmentMessage downloading);

    void setErrorDownloading(Environment environment, BuildException e);

    void setDownloadStartedTo(Environment environment, String localPath);

    void setFileSizeCalculated(Environment environment, String size);

    void setErrorSizeCalculation(Environment environment, Exception e);

    void clear();

    EnvironmentStatus getStatus(Long envId);
}
