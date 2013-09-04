package org.xendan.logmonitor.dao;

import org.xendan.logmonitor.model.LogMonitorConfiguration;

/**
 * User: id967161
 * Date: 04/09/13
 */
public interface LogMonitorSettingsDao {
    LogMonitorConfiguration getConfig(String name);

    void save(LogMonitorConfiguration config);
}
