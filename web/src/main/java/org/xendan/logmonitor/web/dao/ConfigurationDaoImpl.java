package org.xendan.logmonitor.web.dao;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import org.xendan.logmonitor.model.*;

import javax.persistence.EntityManager;
import java.util.Collections;
import java.util.List;

/**
 * @author xendan
 * @since 04/09/13
 */
@SuppressWarnings("unchecked")
@Singleton
@Transactional
public class ConfigurationDaoImpl implements ConfigurationDao {
	
//    protected EntityManager entityManager;
    private final Provider<EntityManager> emProvider;

    @Inject
    public ConfigurationDaoImpl(Provider<EntityManager> emProvider) {
        this.emProvider = emProvider;
    }

    @Override
    public void persist(BaseObject baseObject) {
        getEntityManager().persist(baseObject);
    }

    private EntityManager getEntityManager() {
        return emProvider.get();
    }

    @Override
    public void merge(BaseObject object) {
        /*
        if (object instanceof Configuration) {
            Configuration config = (Configuration) object;
            for (Environment environment : config.getEnvironments()) {
                for (MatchConfig matchConfig : environment.getMatchConfigs()) {
                    merge(matchConfig);
                }
            }
        }*/
        getEntityManager().merge(object);
    }

    public void clearAll() {
        getEntityManager().createNativeQuery("DROP ALL OBJECTS ").executeUpdate();
    }

    @Override
    public synchronized List<Configuration> getConfigs() {
        List<Configuration> configs = getAll(Configuration.class);
        for (Configuration configuration : configs) {
            sortEnvironments(configuration);
        }
        return configs;
    }

    private Configuration sortEnvironments(Configuration configuration) {
        //TODO why not done by hibernate..
        for (Environment environment : configuration.getEnvironments()) {
            Collections.sort(environment.getMatchConfigs());
        }
        return configuration;
    }

    @Override
    public Configuration getConfig(Long configId) {
        return sortEnvironments(getEntityManager().find(Configuration.class, configId));
    }

    private <T> List<T> getAll(Class<T> entityClass) {
        return getEntityManager().createQuery("SELECT l FROM " + entityClass.getName() + " l", entityClass)
                .getResultList();

    }


    @Override
    public void remove(BaseObject object) {
        getEntityManager().remove(object);

    }

    @Override
    public void removeAllEntries(Environment environment) {
        synchronized (ConfigurationDaoImpl.class) {
            //TODO maybe it is possible by JPA
            getEntityManager().createNativeQuery("DELETE FROM LOG_ENTRY_GROUP_ENTRIES " +
                    "WHERE ENTRIES IN (" +
                    "SELECT ID FROM LOG_ENTRY " +
                    " WHERE ENVIRONMENT = ?" +

                    ")").setParameter(1, environment.getId())
                    .executeUpdate();
            getEntityManager().createNativeQuery("DELETE FROM LOG_ENTRY_GROUP g " +
                    " WHERE NOT EXISTS (" +
                    "SELECT LOG_ENTRY_GROUP " +
                    " FROM LOG_ENTRY_GROUP_ENTRIES lg " +
                    " WHERE lg.LOG_ENTRY_GROUP = g.ID" +
                    ")").executeUpdate();
            getEntityManager().createNativeQuery("DELETE FROM LOG_ENTRY  WHERE ENVIRONMENT = :environment")
                    .setParameter("environment", environment.getId())

                    .executeUpdate();
        }
    }

    @Override
    public void removeMatchConfig(Environment environment, MatchConfig config) {
        getEntityManager()
                .createQuery("DELETE FROM LogEntry WHERE matchConfig = :config AND environment = :environment")
                .setParameter("config", config)
                .setParameter("environment", environment)
                .executeUpdate();
        getEntityManager()
                .createNativeQuery("DELETE FROM MATCH_CONFIG c " +
                        "WHERE NOT EXISTS (" +
                        "SELECT MATCH_CONFIGS " +
                        "FROM ENVIRONMENT_MATCH_CONFIGS " +
                        "WHERE MATCH_CONFIGS = c.ID" +
                        ")")
                .executeUpdate();
    }

    @Override
    public List<LogEntry> getNotGroupedMatchedEntries(Long matchConfigId, Long environmentId) {
//        System.out.println(entityManager.createQuery("from LogEntry").getResultList());
        return getEntityManager().createNativeQuery(
                "SELECT e.* FROM LOG_ENTRY e " +
                        " WHERE e.MATCH_CONFIG = (:matcher) AND e.ENVIRONMENT = (:environment) " +
                        " AND NOT EXISTS (" +
                        "SELECT ENTRIES " +
                        "FROM  LOG_ENTRY_GROUP_ENTRIES " +
                        "WHERE ENTRIES = e.ID" +
                        ")" +
                        " ORDER BY e.date DESC ",
                LogEntry.class
        )
                .setParameter("matcher", matchConfigId)
                .setParameter("environment", environmentId)
                .getResultList();
    }


    @Override
    public List<LogEntryGroup> getMatchedEntryGroups(Long matchConfigId, Long environmentId) {
//        System.out.println(entityManager.createQuery("from LogEntryGroup").getResultList());
        return getEntityManager().createNativeQuery(
                "SELECT g.* FROM LOG_ENTRY_GROUP g" +
                        " WHERE EXISTS (" +
                        "    SELECT 1 FROM  LOG_ENTRY_GROUP_ENTRIES le, LOG_ENTRY e" +
                        "    WHERE le.LOG_ENTRY_GROUP = g.ID " +
                        "    AND le.ENTRIES = e.ID" +
                        "    AND e.MATCH_CONFIG = (:matcher) " +
                        "   AND e.ENVIRONMENT = (:environment) )",
                LogEntryGroup.class
        )
                .setParameter("matcher", matchConfigId)
                .setParameter("environment", environmentId)
                .getResultList();
    }

    @Override
    public List<Server> getAllServers() {
        return getEntityManager().createQuery("select s from Server s", Server.class).getResultList();
    }
}
