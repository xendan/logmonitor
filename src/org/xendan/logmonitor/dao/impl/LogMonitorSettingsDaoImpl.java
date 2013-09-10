package org.xendan.logmonitor.dao.impl;

import com.intellij.openapi.components.ServiceManager;
import org.hibernate.ejb.Ejb3Configuration;
import org.hibernate.ejb.HibernatePersistence;
import org.hibernate.ejb.packaging.PersistenceMetadata;
import org.xendan.logmonitor.HomeResolver;
import org.xendan.logmonitor.dao.LogMonitorSettingsDao;
import org.xendan.logmonitor.model.LogEntry;
import org.xendan.logmonitor.model.LogMonitorConfiguration;
import org.xendan.logmonitor.model.MatchConfig;
import org.xendan.logmonitor.model.ServerSettings;

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
public class LogMonitorSettingsDaoImpl implements LogMonitorSettingsDao {

    private final EntityManager entityManager;

    public LogMonitorSettingsDaoImpl() {
        this(createUnit());
    }

    private static EntityManager createUnit() {
        PersistenceProviderResolverHolder.setPersistenceProviderResolver(new IdeaPersistenceProviderResolver());
        return Persistence.createEntityManagerFactory("defaultPersistentUnit").createEntityManager();
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public LogMonitorSettingsDaoImpl(EntityManager entityManager) {

        this.entityManager = entityManager;
    }


    @Override
    public LogMonitorConfiguration getConfig(String projectName) {
        List<LogMonitorConfiguration> configs = entityManager.createQuery("SELECT l FROM LogMonitorConfiguration l where projectName = :name", LogMonitorConfiguration.class)
                .setParameter("name", projectName)
                .getResultList();
        if (configs.isEmpty()) {
            LogMonitorConfiguration configuration = new LogMonitorConfiguration();
            configuration.setProjectName(projectName);
            return configuration;
        }
        configs.get(0).getServerSettings().size();
        return configs.get(0);
    }

    @Override
    public List<LogMonitorConfiguration> getConfigs() {
        entityManager.getTransaction().begin();
        List<LogMonitorConfiguration> configs = entityManager.createQuery("SELECT l FROM LogMonitorConfiguration l", LogMonitorConfiguration.class)
                .getResultList();
        for (LogMonitorConfiguration config : configs) {
            config.getServerSettings().size();
        }
        entityManager.getTransaction().commit();
        return configs;
    }

    @Override
    public void save(LogMonitorConfiguration config) {
        entityManager.getTransaction().begin();
        entityManager.merge(config);
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
                        LogMonitorConfiguration.class.getName(),
                        ServerSettings.class.getName(),
                        MatchConfig.class.getName(),
                        LogEntry.class.getName())
                        );
                Properties props = new Properties();
                props.setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
                props.setProperty("hibernate.connection.driver_class", "org.h2.Driver");
                props.setProperty("hibernate.connection.password", "admin");
                props.setProperty("hibernate.connection.username", "admin");
                props.setProperty("hibernate.hbm2ddl.auto", "update");
                props.setProperty("hibernate.connection.url", "jdbc:h2:/" + ServiceManager.getService(HomeResolver.class).getPath("db"));
                metadata.setProps(props);
                Ejb3Configuration configured = cfg.configure(metadata, properties );
                return configured != null ? configured.buildEntityManagerFactory() : null;
            }
        }
    }
}
