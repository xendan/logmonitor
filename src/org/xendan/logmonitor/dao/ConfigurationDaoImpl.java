package org.xendan.logmonitor.dao;

import org.xendan.logmonitor.model.*;

import javax.persistence.EntityManager;
import java.util.Collections;
import java.util.List;

/**
 * User: id967161
 * Date: 04/09/13
 */
@SuppressWarnings("unchecked")
public class ConfigurationDaoImpl implements ConfigurationDao {

    protected EntityManager entityManager;

    public ConfigurationDaoImpl(EntityManager entityManager) {
       this.entityManager = entityManager;
    }


    @Override
    public void persist(BaseObject baseObject) {
        if (baseObject instanceof LogEntry) {
            LogEntry entry = (LogEntry) baseObject;
            assert entry.getDate() != null;
            assert entry.getMessage() != null;
        }
        entityManager.persist(baseObject);
    }

    public void clearAll() {
        entityManager.createNativeQuery("DROP ALL OBJECTS ").executeUpdate();
    }

    @Override
    public void startTransaction() {
        if (!entityManager.getTransaction().isActive()) {
            entityManager.getTransaction().begin();
        }
    }

    @Override
    public void commit() {
        if (entityManager.getTransaction().isActive()) {
            entityManager.getTransaction().commit();
        }
    }

    @Override
    public synchronized List<Configuration> getConfigs() {
        List<Configuration> configs = getAll(Configuration.class);
        for (Configuration configuration : configs) {
            //TODO why not done by hibernate..
            for (Environment environment : configuration.getEnvironments()) {
                Collections.sort(environment.getMatchConfigs());
            }
        }
        return configs;
    }

    private <T> List<T> getAll(Class<T> entityClass) {
        return entityManager.createQuery("SELECT l FROM " + entityClass.getName() + " l", entityClass)
                .getResultList();

    }


    @Override
    public void remove(BaseObject object) {
        entityManager.remove(object);

    }

    @Override
    public void removeAllEntries(Environment environment) {
        synchronized (ConfigurationDaoImpl.class) {
            //TODO maybe it is possible by JPA
            entityManager.createNativeQuery("DELETE FROM LOG_ENTRY_GROUP_ENTRIES " +
                    "WHERE ENTRIES IN (" +
                    "SELECT ID FROM LOG_ENTRY " +
                    " WHERE ENVIRONMENT = ?" +

                    ")").setParameter(1, environment.getId())
                    .executeUpdate();
            entityManager.createNativeQuery("DELETE FROM LOG_ENTRY_GROUP g " +
                    " WHERE NOT EXISTS (" +
                    "SELECT LOG_ENTRY_GROUP " +
                    " FROM LOG_ENTRY_GROUP_ENTRIES lg " +
                    " WHERE lg.LOG_ENTRY_GROUP = g.ID" +
                    ")").executeUpdate();
            LogEntry last = getLastEntry(environment);
            entityManager.createNativeQuery("DELETE FROM LOG_ENTRY  WHERE ENVIRONMENT = ? AND ID <> ?")
                    .setParameter(1, environment.getId())
                    .setParameter(2, last.getId())
                    .executeUpdate();
        }
    }

    @Override
    public void removeMatchConfig(Environment environment, MatchConfig config) {
        entityManager
                .createQuery("DELETE FROM LogEntry WHERE matchConfig = :config AND environment = :environment")
                .setParameter("config", config)
                .setParameter("environment", environment)
                .executeUpdate();
        entityManager
                .createNativeQuery("DELETE FROM MATCH_CONFIG c " +
                        "WHERE NOT EXISTS (" +
                        "SELECT MATCH_CONFIGS " +
                        "FROM ENVIRONMENT_MATCH_CONFIGS " +
                        "WHERE MATCH_CONFIGS = c.ID" +
                        ")")
                .executeUpdate();
    }

    @Override
    public LogEntry getLastEntry(Environment environment) {
        List<LogEntry> entries = entityManager.createQuery(
                "SELECT e FROM LogEntry e WHERE e.environment = (:environment) ORDER BY e.date DESC ",
                LogEntry.class)
                .setParameter("environment", environment)
                .setMaxResults(1)
                .getResultList();
        if (entries.isEmpty()) {
            return null;
        }
        return entries.get(0);
    }

    @Override
    public List<LogEntry> getNotGroupedMatchedEntries(MatchConfig matchConfig, Environment environment) {
        return entityManager.createNativeQuery(
                "SELECT e.* FROM LOG_ENTRY e " +
                        " WHERE e.MATCH_CONFIG = (:matcher) AND e.ENVIRONMENT = (:environment) " +
                        " AND NOT EXISTS (" +
                        "SELECT ENTRIES " +
                        "FROM  LOG_ENTRY_GROUP_ENTRIES " +
                        "WHERE ENTRIES = e.ID" +
                        ")" +
                        " ORDER BY e.date DESC ",
                LogEntry.class)
                .setParameter("matcher", matchConfig.getId())
                .setParameter("environment", environment.getId())
                .getResultList();
    }


    @Override
    public List<LogEntryGroup> getMatchedEntryGroups(MatchConfig matchConfig, Environment environment) {
        return entityManager.createNativeQuery(
                "SELECT g.* FROM LOG_ENTRY_GROUP g" +
                        " WHERE EXISTS (" +
                        "    SELECT 1 FROM  LOG_ENTRY_GROUP_ENTRIES le, LOG_ENTRY e" +
                        "    WHERE le.LOG_ENTRY_GROUP = g.ID " +
                        "    AND le.ENTRIES = e.ID" +
                        "    AND e.MATCH_CONFIG = (:matcher) " +
                        "   AND e.ENVIRONMENT = (:environment) )",
                LogEntryGroup.class)
                .setParameter("matcher", matchConfig.getId())
                .setParameter("environment", environment.getId())
                .getResultList();
    }
}
