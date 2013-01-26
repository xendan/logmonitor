package org.xendan.logmonitor;

import org.xendan.logmonitor.model.HostSettings;
import org.xendan.logmonitor.model.LogErrorData;

/**
 * User: kcyxa
 * Date: 1/26/13
 */
public interface LogErrorDao {

    void updateErrorData(LogErrorData logErrorData);

    LogErrorData getErrorData(HostSettings settings);
}
