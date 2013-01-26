package org.xendan.logmonitor;

import org.xendan.logmonitor.model.HostSettings;
import org.xendan.logmonitor.model.LogEntry;
import org.xendan.logmonitor.model.LogErrorData;

import java.util.List;

/**
 * User: kcyxa
 * Date: 1/26/13
 */
public class LogErrorsService {

    private LogErrorDao dao;

    public LogErrorsService(LogErrorDao dao) {
        this.dao = dao;
    }

    public void updateErrors(LogErrorData data, List<LogEntry> entries) {
       dao.updateErrorData(data);
    }

    public LogErrorData getLogErrorData(HostSettings settings) {
        return dao.getErrorData(settings);
    }
}
