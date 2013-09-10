package org.xendan.logmonitor.dao;

import org.xendan.logmonitor.model.LogMonitorConfiguration;

import javax.persistence.EntityManager;
import java.util.List;

/**
 * User: id967161
 * Date: 04/09/13
 */
public interface LogMonitorSettingsDao {
    LogMonitorConfiguration getConfig(String name);

    List<LogMonitorConfiguration> getConfigs();

    void save(LogMonitorConfiguration config);

    EntityManager getEntityManager();
}
