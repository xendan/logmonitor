package org.xendan.logmonitor.dao.impl;

import org.hibernate.ejb.Ejb3Configuration;
import org.hibernate.ejb.HibernatePersistence;
import org.hibernate.ejb.packaging.PersistenceMetadata;
import org.xendan.logmonitor.HomeResolver;
import org.xendan.logmonitor.dao.ConfigurationDao;
import org.xendan.logmonitor.model.*;
import org.xendan.logmonitor.parser.LogParser;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceProviderResolver;
import javax.persistence.spi.PersistenceProviderResolverHolder;
import javax.persistence.spi.PersistenceUnitTransactionType;
import java.util.*;
import java.util.regex.Pattern;

/**
 * User: id967161
 * Date: 04/09/13
 */
public class ConfigurationDaoImpl implements ConfigurationDao {

    private final EntityManager entityManager;

    public ConfigurationDaoImpl(HomeResolver homeResolver) {
        this(createUnit(homeResolver, "db"));
    }

    public ConfigurationDaoImpl(HomeResolver homeResolver, String dbPath) {
        this(createUnit(homeResolver, dbPath));
    }

    private static EntityManager createUnit(HomeResolver homeResolver, String dbPath) {
        PersistenceProviderResolverHolder.setPersistenceProviderResolver(new IdeaPersistenceProviderResolver(homeResolver, dbPath));
        return Persistence.createEntityManagerFactory("defaultPersistentUnit").createEntityManager();
    }

    public ConfigurationDaoImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }


    private void initMatcherException(Configuration configuration) {
        for (Environment environment : configuration.getEnvironments()) {
            for (MatchConfig matchConfig : environment.getMatchConfigs()) {
                matchConfig.getExceptions().size();
            }
        }
    }

    @Override
    public List<Configuration> getConfigs() {
        List<Configuration> configs = getAll(Configuration.class);
        for (Configuration configuration : configs) {
            initMatcherException(configuration);
        }
        return configs;
    }

    private <T> List<T> getAll(Class<T> entityClass) {
        return entityManager.createQuery("SELECT l FROM " + entityClass.getName() + " l", entityClass)
                .getResultList();

    }

    @Override
    public void save(List<Configuration> configs) {
        entityManager.getTransaction().begin();
        for (Configuration config : configs) {
            entityManager.persist(config);
        }
        entityManager.getTransaction().commit();

    }

    @Override
    public LogEntry getLastEntry(Environment environment) {
        List<LogEntry> entries = entityManager.createQuery(
                "SELECT e FROM LogEntry e where e.environment = (:environment) ORDER BY e.date DESC ",
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
    public List<LogEntry> getMatchedEntries(MatchConfig matchConfig, Environment environment) {
        return entityManager.createQuery(
                "SELECT e FROM LogEntry e where e.matchConfig = (:matcher) AND e.environment = (:environment)  ORDER BY e.date DESC ",
                LogEntry.class)
                .setParameter("matcher", matchConfig)
                .setParameter("environment", environment)
                .getResultList();
    }


    private List<LogEntry> getMatchedNotGroupEntries(MatchConfig matchConfig, Environment environment) {
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

    @Override
    public void clearEntries(Environment environment) {
        entityManager.getTransaction().begin();
        entityManager.createQuery(
                "DELETE FROM LogEntry e where e.matchConfig = (:matchers) ")
                .setParameter("matchers", environment.getMatchConfigs())
                .executeUpdate();
        entityManager.getTransaction().commit();
    }

    @Override
    public void addMatchConfig(MatchConfig matcher, MatchConfig parentMatcher, Environment settings) {
        entityManager.getTransaction().begin();
        matcher.setWeight(getMaxWeight(settings.getMatchConfigs()) + 1);
        settings.getMatchConfigs().add(matcher);
        entityManager.persist(settings);
        /*
        if (matcher.isUseArchive()) {
            List<LogEntry> entries = entityManager.createQuery(
                    "SELECT e FROM LogEntry e where e.matchConfig = (:matcher) ORDER BY e.date DESC ",
                    LogEntry.class)
                    .setParameter("matcher", parentMatcher)
                    .getResultList();
            List<LogEntry> filtered = new ArrayList<LogEntry>();
            /*
            EntryMatcher entryMatcher = new EntryMatcher(Arrays.asList(matcher));
            for (LogEntry entry : entries) {
                if (entryMatcher.match(entry)) {
                    filtered.add(entry);
                    entityManager.remove(entry);
                }
            }
            if (!filtered.isEmpty()) {
                entityManager.merge(filtered.get(0).createCopy(filtered.size(), matcher));
            }

        }
    */
        entityManager.getTransaction().commit();
    }

    private int getMaxWeight(List<MatchConfig> matchConfigs) {
        Collections.sort(matchConfigs);
        if (matchConfigs.isEmpty()) {
            return 0;
        }
        return matchConfigs.get(0).getWeight() == null ? 0 : matchConfigs.get(0).getWeight();
    }

    @Override
    public void addEntries(List<LogEntry> entries) {
        entityManager.getTransaction().begin();
        for (LogEntry entry : entries) {
            if (!entry.getMatchConfig().isGeneral()) {
                List<LogEntry> oldEntries = getMatchedEntries(entry.getMatchConfig(), entry.getEnvironment());
                if (!oldEntries.isEmpty()) {
                    LogEntry first = oldEntries.get(0);
                    first.setDate(entry.getDate());
                    first.setFoundNumber(first.getFoundNumber() + 1);
                    for (int i = 1; i < entries.size(); i++) {
                        entityManager.remove(entries.get(i));
                    }
                    entry = first;
                }
                entityManager.persist(entry);
            } else {
                List<LogEntryGroup> groups = getMatchedEntryGroups(entry.getMatchConfig(), entry.getEnvironment());
                LogEntryGroup matchedGroup = getMatchedGroup(groups, entry);
                if (matchedGroup != null) {
                    entry.setMessage("");
                    matchedGroup.getEntries().add(entry);
                    entityManager.persist(matchedGroup);
                } else {
                    List<LogEntry> oldEntries = getMatchedNotGroupEntries(entry.getMatchConfig(), entry.getEnvironment());
                    boolean matchFound = false;
                    for (LogEntry oldEntry : oldEntries) {
                        if (oldEntry.getMessage().equals(entry.getMessage())) {
                            LogEntryGroup group = new LogEntryGroup();
                            group.setMessagePattern(LogParser.replaceSpecial(entry.getMessage()));
                            oldEntry.setMessage("");
                            entry.setMessage("");
                            group.getEntries().add(oldEntry);
                            group.getEntries().add(entry);
                            entityManager.persist(group);
                            entityManager.flush();
                            matchFound = true;
                            break;
                        }

                    }
                    if (!matchFound) {
                        entityManager.persist(entry);
                    }
                }

            }
        }
        entityManager.getTransaction().commit();
    }




    private LogEntryGroup getMatchedGroup(List<LogEntryGroup> groups, LogEntry entry) {
        for (LogEntryGroup group : groups) {
            if (Pattern.matches(group.getMessagePattern(), entry.getMessage())) {
                return group;
            }
        }
        return null;
    }


    private static class IdeaPersistenceProviderResolver implements PersistenceProviderResolver {
        private final HomeResolver homeResolver;
        private final String dbpath;
        private PersistenceProvider ideaPersistenceProvider = new IdeaPersistenceProvider();

        public IdeaPersistenceProviderResolver(HomeResolver homeResolver, String dbpath) {
            this.homeResolver = homeResolver;
            this.dbpath = dbpath;
        }

        @Override
        public List<PersistenceProvider> getPersistenceProviders() {
            return Arrays.asList(ideaPersistenceProvider);
        }

        @Override
        public void clearCachedProviders() {
        }

        private class IdeaPersistenceProvider extends HibernatePersistence {

            @Override
            public EntityManagerFactory createEntityManagerFactory(String persistenceUnitName, Map properties) {
                Ejb3Configuration cfg = new Ejb3Configuration();
                PersistenceMetadata metadata = new PersistenceMetadata();
                metadata.setName(persistenceUnitName);
                metadata.setTransactionType(PersistenceUnitTransactionType.RESOURCE_LOCAL);
                metadata.setClasses(Arrays.asList(
                        Configuration.class.getName(),
                        Environment.class.getName(),
                        MatchConfig.class.getName(),
                        Server.class.getName(),
                        LogEntry.class.getName(),
                        LogEntryGroup.class.getName())
                );
                Properties props = new Properties();
                props.setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
                props.setProperty("hibernate.connection.driver_class", "org.h2.Driver");
                props.setProperty("hibernate.connection.password", "admin");
                props.setProperty("hibernate.connection.username", "admin");
                props.setProperty("hibernate.hbm2ddl.auto", "update");
                props.setProperty("hibernate.ejb.naming_strategy", "org.hibernate.cfg.ImprovedNamingStrategy");
                String connection = "jdbc:h2:/" + homeResolver.joinMkDirs("db", dbpath);
                System.out.println(connection);
                props.setProperty("hibernate.connection.url", connection);
                metadata.setProps(props);
                Ejb3Configuration configured = cfg.configure(metadata, properties);
                return configured != null ? configured.buildEntityManagerFactory() : null;
            }
        }
    }
}
