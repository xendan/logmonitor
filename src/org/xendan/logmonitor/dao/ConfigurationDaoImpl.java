package org.xendan.logmonitor.dao;

import org.hibernate.ejb.HibernatePersistence;
import org.hibernate.internal.util.ClassLoaderHelper;
import org.xendan.logmonitor.HomeResolver;
import org.xendan.logmonitor.model.*;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceProviderResolver;
import javax.persistence.spi.PersistenceProviderResolverHolder;
import java.util.*;

/**
 * User: id967161
 * Date: 04/09/13
 */
@SuppressWarnings("unchecked")
public class ConfigurationDaoImpl implements ConfigurationDao {

    public static final String DEF_PATH = "db";
    private final String dbPath;
    protected EntityManager entityManager;
    private final HomeResolver homeResolver;

    protected ConfigurationDaoImpl(HomeResolver homeResolver) {
        this(homeResolver, DEF_PATH);
    }

    protected ConfigurationDaoImpl(HomeResolver homeResolver, String dbPath) {
        this(createUnit(homeResolver, dbPath), homeResolver, dbPath);
    }

    private static EntityManager createUnit(HomeResolver homeResolver, String dbPath) {
        PersistenceProviderResolverHolder.setPersistenceProviderResolver(new IdeaPersistenceProviderResolver());
        ClassLoaderHelper.overridenClassLoader = ConfigurationDaoImpl.class.getClassLoader();
        return Persistence.createEntityManagerFactory("defaultPersistentUnit").createEntityManager(createProperties(homeResolver, dbPath));
    }

    private static Map<String, String> createProperties(HomeResolver homeResolver, String dbPath) {
        Map<String, String> props = new HashMap<String, String>();
        props.put("hibernate.connection.url", createConnectionStr(homeResolver, dbPath));
        return props;
    }

    private static String createConnectionStr(HomeResolver homeResolver, String dbPath) {
        String connection = "jdbc:h2:/" + homeResolver.joinMkDirs(DEF_PATH, dbPath) + ";MVCC=true";
        System.out.println(Thread.currentThread());
        System.out.println(connection);
        return connection;
    }

    private ConfigurationDaoImpl(EntityManager entityManager, HomeResolver homeResolver, String dbPath) {
        this.entityManager = entityManager;
        this.homeResolver = homeResolver;
        this.dbPath = dbPath;
    }

    @Override
    public void persist(BaseObject baseObject) {
        if (baseObject instanceof LogEntry) {
            LogEntry entry = (LogEntry) baseObject;
            assert entry.getDate() != null;
            assert entry.getMessage() != null;
        }
        entityManager.merge(baseObject);
    }

    public void clearAll() {
        entityManager.createNativeQuery("DROP ALL OBJECTS ").executeUpdate();
        entityManager.getTransaction().commit();
        entityManager = createUnit(homeResolver, dbPath);

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

    private static class IdeaPersistenceProviderResolver implements PersistenceProviderResolver {

        @Override
        public List<PersistenceProvider> getPersistenceProviders() {
            List<PersistenceProvider> list = new ArrayList<PersistenceProvider>();
            list.add(new HibernatePersistence());
            return list;
        }

        @Override
        public void clearCachedProviders() {
        }


    }
}
