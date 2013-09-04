package org.xendan.logmonitor;

import org.xendan.logmonitor.model.LogErrorData;
import org.xendan.logmonitor.model.ServerSettings;

/**
 * User: kcyxa
 * Date: 1/26/13
 */
public interface LogErrorDao {

    void updateErrorData(LogErrorData logErrorData);

    LogErrorData getErrorData(ServerSettings settings);
}
