package org.xendan.logmonitor.web.guice;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.persist.PersistService;
import com.google.inject.persist.jpa.JpaPersistModule;
import com.google.inject.servlet.GuiceServletContextListener;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.xendan.logmonitor.HomeResolver;
import org.xendan.logmonitor.web.dao.ConfigurationDao;
import org.xendan.logmonitor.web.dao.ConfigurationDaoImpl;
import org.xendan.logmonitor.web.read.schedule.ReaderScheduler;
import org.xendan.logmonitor.web.service.*;
import org.xendan.logmonitor.web.service.resources.ConfigResource;
import org.xendan.logmonitor.web.service.resources.LogEntryResource;
import org.xendan.logmonitor.web.service.resources.LogLevelResource;
import org.xendan.logmonitor.web.service.resources.ServersResource;

import javax.servlet.ServletContextEvent;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class GuiceServletConfig extends GuiceServletContextListener {

    private static final String DEF_PATH = "db";
    public static final String PERSISTENT_UNIT = "defaultPersistentUnit";
    private Injector injector;

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        super.contextInitialized(servletContextEvent);
        injector.getInstance(PersistService.class).start();
        ReaderScheduler scheduler = injector.getInstance(ReaderScheduler.class);
        scheduler.reload();
    }

    @Override
	protected Injector getInjector() {
        String dbFile = DEF_PATH;
        injector = createInjector(dbFile);
        return injector;
	}

    public static Injector createInjector(final String dbFile) {
        return Guice.createInjector(new JerseyServletModule() {
            @Override
            protected void configureServlets() {
                JpaPersistModule jpaModule = new JpaPersistModule(PERSISTENT_UNIT);
                JpaPersistModule module = jpaModule.properties(createJpaProperties(dbFile));
                install(module);
                bind(ConfigResource.class);
                bind(LogEntryResource.class);
                bind(LogLevelResource.class);
                bind(ServersResource.class);
                bind(HomeResolver.class);
                bind(ConfigurationDao.class).to(ConfigurationDaoImpl.class);
                bind(ReaderScheduler.class);
                bind(EnvironmentMonitor.class).to(EnvironmentMonitorImpl.class);
                bind(MessageBodyReader.class).to(JacksonJsonProvider.class);
                bind(MessageBodyWriter.class).to(JacksonJsonProvider.class);
                bind(LogService.class).toProvider(LogServiceProvider.class);
                filter("/*").through(SpecialPersistFilter.class);
                serve("/*").with(GuiceContainer.class, createJsonParams());
            }
        });
    }

    public static Properties createJpaProperties(String dbFile) {
        Properties props = new Properties();
        props.put("hibernate.connection.url", createConnectionStr(new HomeResolver(), dbFile));
        return props;
    }

    private static String createConnectionStr(HomeResolver homeResolver, String dbFile) {
        String connection = "jdbc:h2:/" + homeResolver.joinMkDirs(dbFile, DEF_PATH) + ";MVCC=true";
        System.out.println(connection);
        return connection;
    }

    private static Map<String, String> createJsonParams() {
        Map<String, String> params = new HashMap<String, String>();
        params.put(JSONConfiguration.FEATURE_POJO_MAPPING, "true");
        return params;
    }
}
