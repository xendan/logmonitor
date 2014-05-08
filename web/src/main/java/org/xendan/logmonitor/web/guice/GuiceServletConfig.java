package org.xendan.logmonitor.web.guice;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.persist.PersistFilter;
import com.google.inject.persist.jpa.JpaPersistModule;
import com.google.inject.servlet.GuiceServletContextListener;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.xendan.logmonitor.HomeResolver;
import org.xendan.logmonitor.web.dao.ConfigurationDao;
import org.xendan.logmonitor.web.dao.ConfigurationDaoImpl;
import org.xendan.logmonitor.web.service.ConfigResource;
import org.xendan.logmonitor.web.service.LogLevelResource;
import org.xendan.logmonitor.web.service.LogService;
import org.xendan.logmonitor.web.service.ServersResource;

import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class GuiceServletConfig extends GuiceServletContextListener {

    private static final String DEF_PATH = "db";

	@Override
	protected Injector getInjector() {
        return Guice.createInjector(new JerseyServletModule() {
            @Override
            protected void configureServlets() {
                JpaPersistModule jpaModule = new JpaPersistModule("defaultPersistentUnit");
                JpaPersistModule module = jpaModule.properties(createJpaProperties());
                install(module);
                bind(ConfigResource.class);
                bind(LogLevelResource.class);
                bind(ServersResource.class);
                bind(HomeResolver.class);
                bind(ConfigurationDao.class).to(ConfigurationDaoImpl.class);
                bind(MessageBodyReader.class).to(JacksonJsonProvider.class);
                bind(MessageBodyWriter.class).to(JacksonJsonProvider.class);
                bind(LogService.class).toProvider(LogServiceProvider.class);
                filter("/*").through(PersistFilter.class);
                serve("/*").with(GuiceContainer.class, createJsonParams());
            }
        });
	}

    private Properties createJpaProperties() {
        Properties props = new Properties();
        props.put("hibernate.connection.url", createConnectionStr(new HomeResolver()));
        return props;
    }

    private String createConnectionStr(HomeResolver homeResolver) {
        String connection = "jdbc:h2:/" + homeResolver.joinMkDirs(DEF_PATH, DEF_PATH) + ";MVCC=true";
        System.out.println(connection);
        return connection;
    }

    private Map<String, String> createJsonParams() {
        Map<String, String> params = new HashMap<String, String>();
        params.put(JSONConfiguration.FEATURE_POJO_MAPPING, "true");
        return params;
    }
}
