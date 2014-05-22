package org.xendan.logmonitor.web.dao;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDateTime;
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

    private final Provider<EntityManager> emProvider;

    @Inject
    public ConfigurationDaoImpl(Provider<EntityManager> emProvider) {
        this.emProvider = emProvider;
    }

    @Override
    public void persist(BaseObject baseObject) {
        if (baseObject instanceof LogEntry) {
          LogEntry entry = (LogEntry) baseObject;
          if (StringUtils.isEmpty(entry.getMessage())) {
              List groups = getEntityManager()
                      .createQuery("SELECT g FROM LogEntryGroup g " +
                              " JOIN g.entries e" +
                              " WHERE e = :entry ")
                      .setParameter("entry", entry)
                      .getResultList();
              if (groups.size() == 0) {
                  System.out.println("OMG!!!");
              }
          }
        }
        getEntityManager().persist(baseObject);
    }

    private EntityManager getEntityManager() {
        return emProvider.get();
    }

    @Override
    public void merge(BaseObject object) {
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
        //TODO also problems with eager environment load
        for (Environment environment : configuration.getEnvironments()) {
            Collections.sort(environment.getMatchConfigs());
            environment.getMatchConfigs().size();
        }
        return configuration;
    }

    @Override
    public Configuration getConfig(Long configId) {
        return sortEnvironments(getEntityManager().find(Configuration.class, configId));
    }

    @Override
    public Configuration getConfigByEnvironment(Long envId) {
        return sortEnvironments((Configuration)
                getEntityManager().createQuery("SELECT c " +
                        " FROM Configuration c " +
                        " JOIN c.environments e " +
                        " WHERE e.id = :environment")
                .setParameter("environment", envId)
                .getSingleResult());
    }

    @Override
    public Environment getEnvironment(long environmentId) {
        return getEntityManager().find(Environment.class, environmentId);
    }

    private <T> List<T> getAll(Class<T> entityClass) {
        return getEntityManager()
                .createQuery("SELECT l FROM " + entityClass.getName() + " l", entityClass)
                .getResultList();
    }

    @Override
    public void remove(BaseObject object) {
        getEntityManager().remove(object);
    }

    @Override
    public void removeAllEntries(Long envId, List<Long> matchersId) {
        getEntityManager().createQuery("DELETE FROM LogEntryGroup g " +
                " WHERE g IN (" +
                "SELECT g0 FROM LogEntryGroup g0 " +
                " JOIN g0.entries e WHERE (" +
                "  e.matchConfig.id IN :matchersId" +
                " AND e.environment.id = :environment ))")
                .setParameter("environment", envId)
                .setParameter("matchersId", matchersId)
                .executeUpdate();

        getEntityManager().createQuery("DELETE FROM LogEntry e  " +
                " WHERE e.environment.id = :environment" +
                " AND e.matchConfig.id IN :matchersId")
                .setParameter("environment", envId)
                .setParameter("matchersId", matchersId)
                .executeUpdate();
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
    public List<LogEntry> getNotGroupedEntries(Long matchConfigId, Long environmentId, LocalDateTime last) {
        return getEntityManager().createQuery(
                "SELECT e FROM LogEntry e " +
                        " WHERE e.matchConfig.id = (:matcher) " +
                        " AND e.environment.id= (:environment) " +
                        " AND e.date > (:last) " +
                        " AND NOT EXISTS (" +
                        " SELECT g FROM LogEntryGroup g " +
                        " JOIN g.entries e0 " +
                        " WHERE e0 = e" +
                        ")" +
                        " ORDER BY e.date DESC "
        )
                .setParameter("matcher", matchConfigId)
                .setParameter("environment", environmentId)
                .setParameter("last", last)
                .getResultList();
    }


    @Override
    public List<LogEntryGroup> getEntryGroups(Long matchConfigId, Long environmentId, LocalDateTime last) {
        return getEntityManager().createQuery(
                "SELECT DISTINCT g FROM LogEntryGroup g" +
                        " JOIN g.entries e " +
                        " WHERE e.matchConfig.id = (:matcher) " +
                        " AND e.environment.id= (:environment) " +
                        " AND e.date > (:last) ")
                .setParameter("matcher", matchConfigId)
                .setParameter("environment", environmentId)
                .setParameter("last", last)
                .getResultList();
    }

    @Override
    public List<Server> getAllServers() {
        return getEntityManager().createQuery("select s from Server s", Server.class).getResultList();
    }
}
