package org.xendan.logmonitor.web;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.xendan.logmonitor.HomeResolver;
import org.xendan.logmonitor.web.dao.ConfigurationDao;
import org.xendan.logmonitor.web.dao.ConfigurationDaoImpl;
import org.xendan.logmonitor.web.service.ConfigResource;

import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import java.util.HashMap;
import java.util.Map;

public class GuiceServletConfig extends GuiceServletContextListener {
	@Override
	protected Injector getInjector() {
        return Guice.createInjector(new JerseyServletModule() {
            @Override
            protected void configureServlets() {
                Map<String, String> initParams = new HashMap<String, String>();
               /*initParams.put(PackagesResourceConfig.PROPERTY_PACKAGES,
                "org.xendan.logmonitor.web.service");
                initParams.put(
                        ServletContainer.RESOURCE_CONFIG_CLASS,
                        ClasspathResourceConfig.class.getName()
                );*/
                bind(ConfigResource.class);
                bind(HomeResolver.class);
                bind(ConfigurationDao.class).to(ConfigurationDaoImpl.class);
                bind(MessageBodyReader.class).to(JacksonJsonProvider.class);
                bind(MessageBodyWriter.class).to(JacksonJsonProvider.class);
               // serve("/*").with(GuiceContainer.class, initParams);
                initParams.put(JSONConfiguration.FEATURE_POJO_MAPPING, "true");
                serve("/*").with(GuiceContainer.class, initParams);
                //filter("/*").through(GuiceContainer.class, initParams);
            }
        });
	}
}
