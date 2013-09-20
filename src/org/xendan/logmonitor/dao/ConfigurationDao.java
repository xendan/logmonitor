package org.xendan.logmonitor.dao;

import org.xendan.logmonitor.model.Configuration;

import javax.persistence.EntityManager;
import java.util.List;

/**
 * User: id967161
 * Date: 04/09/13
 */
public interface ConfigurationDao {


    void save(List<Configuration> configs);

    EntityManager getEntityManager();

    List<Configuration> getConfigs();
}
