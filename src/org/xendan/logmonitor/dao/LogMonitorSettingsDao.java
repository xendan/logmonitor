package org.xendan.logmonitor.dao;

import org.xendan.logmonitor.model.LogMonitorConfiguration;

import java.util.List;

/**
 * User: id967161
 * Date: 04/09/13
 */
public interface LogMonitorSettingsDao {
    LogMonitorConfiguration getConfig(String name);

    List<LogMonitorConfiguration> getConfig();

    void save(LogMonitorConfiguration config);
}
