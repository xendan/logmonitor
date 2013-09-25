package org.xendan.logmonitor.dao.impl;

import com.intellij.openapi.components.ServiceManager;
import org.hibernate.ejb.Ejb3Configuration;
import org.hibernate.ejb.HibernatePersistence;
import org.hibernate.ejb.packaging.PersistenceMetadata;
import org.xendan.logmonitor.HomeResolver;
import org.xendan.logmonitor.dao.ConfigurationDao;
import org.xendan.logmonitor.model.*;
import org.xendan.logmonitor.parser.EntryMatcher;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceProviderResolver;
import javax.persistence.spi.PersistenceProviderResolverHolder;
import javax.persistence.spi.PersistenceUnitTransactionType;
import java.util.*;

/**
 * User: id967161
 * Date: 04/09/13
 */
public class ConfigurationDaoImpl implements ConfigurationDao {

    private final EntityManager entityManager;

    public ConfigurationDaoImpl() {
        this(createUnit());
    }

    private static EntityManager createUnit() {
        PersistenceProviderResolverHolder.setPersistenceProviderResolver(new IdeaPersistenceProviderResolver());
        return Persistence.createEntityManagerFactory("defaultPersistentUnit").createEntityManager();
    }

    public EntityManager getEntityManager() {
        return entityManager;
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
            entityManager.merge(config);
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
    public List<LogEntry> getMatchedEntries(MatchConfig matchConfig) {
        return entityManager.createQuery(
                "SELECT e FROM LogEntry e where e.matchConfig = (:matcher) ORDER BY e.date DESC ",
                LogEntry.class)
                .setParameter("matcher", matchConfig)
                .getResultList();
    }

    @Override
    public void clearEntries(Environment settings) {
        entityManager.getTransaction().begin();
        entityManager.createQuery(
                "DELETE FROM LogEntry e where e.matchConfig = (:matchers) ")
                .setParameter("matchers", settings.getMatchConfigs())
                .executeUpdate();
        entityManager.getTransaction().commit();
    }

    @Override
    public void addMatchConfig(MatchConfig matcher, MatchConfig parentMatcher, Environment settings) {
        entityManager.getTransaction().begin();
        matcher.setWeight(getMaxWeight(settings.getMatchConfigs()) + 1);
        settings.getMatchConfigs().add(matcher);
        entityManager.merge(settings);
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
            entityManager.merge(getEntryToMerge(entry));
        }
        entityManager.getTransaction().commit();
    }

    private LogEntry getEntryToMerge(LogEntry entry) {
        if (entry.getMatchConfig().isGeneral()) {
            return entry;
        }
        List<LogEntry> entries = getMatchedEntries(entry.getMatchConfig());
        if (entries.isEmpty()) {
            return entry;
        }
        LogEntry first = entries.get(0);
        first.setDate(entry.getDate());
        first.setFoundNumber(first.getFoundNumber() + 1);
        for (int i = 1; i < entries.size(); i++) {
            entityManager.remove(entries.get(i));
        }
        return first;
    }

    private static class IdeaPersistenceProviderResolver implements PersistenceProviderResolver {
        private PersistenceProvider ideaPersistenceProvider =  new IdeaPersistenceProvider();

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
                        LogEntry.class.getName())
                        );
                Properties props = new Properties();
                props.setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
                props.setProperty("hibernate.connection.driver_class", "org.h2.Driver");
                props.setProperty("hibernate.connection.password", "admin");
                props.setProperty("hibernate.connection.username", "admin");
                props.setProperty("hibernate.hbm2ddl.auto", "update");
                props.setProperty("hibernate.ejb.naming_strategy", "org.hibernate.cfg.ImprovedNamingStrategy");
                String connection = "jdbc:h2:/" + ServiceManager.getService(HomeResolver.class).joinMkDirs("db", "db");
                System.out.println(connection);
                props.setProperty("hibernate.connection.url", connection);
                metadata.setProps(props);
                Ejb3Configuration configured = cfg.configure(metadata, properties );
                return configured != null ? configured.buildEntityManagerFactory() : null;
            }
        }
    }
}
