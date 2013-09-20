package org.xendan.logmonitor.dao.impl;

import com.intellij.openapi.components.ServiceManager;
import org.hibernate.ejb.Ejb3Configuration;
import org.hibernate.ejb.HibernatePersistence;
import org.hibernate.ejb.packaging.PersistenceMetadata;
import org.xendan.logmonitor.HomeResolver;
import org.xendan.logmonitor.dao.ConfigurationDao;
import org.xendan.logmonitor.model.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceProviderResolver;
import javax.persistence.spi.PersistenceProviderResolverHolder;
import javax.persistence.spi.PersistenceUnitTransactionType;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

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
                props.setProperty("hibernate.connection.url", "jdbc:h2:/" + ServiceManager.getService(HomeResolver.class).joinMkDirs("db", "db"));
                metadata.setProps(props);
                Ejb3Configuration configured = cfg.configure(metadata, properties );
                return configured != null ? configured.buildEntityManagerFactory() : null;
            }
        }
    }
}
