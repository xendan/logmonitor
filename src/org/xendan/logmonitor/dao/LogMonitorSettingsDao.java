package org.xendan.logmonitor.dao;

import org.xendan.logmonitor.model.LogMonitorConfiguration;

import javax.persistence.EntityManager;
import java.util.List;

/**
 * User: id967161
 * Date: 04/09/13
 */
public interface LogMonitorSettingsDao {


    void save(List<LogMonitorConfiguration> configs);

    EntityManager getEntityManager();

    List<LogMonitorConfiguration> getConfigs();
}
