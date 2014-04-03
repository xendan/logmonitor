/**
 * 
 */
package org.xendan.logmonitor.web;

import org.xendan.logmonitor.HomeResolver;
import org.xendan.logmonitor.web.dao.ConfigurationDao;
import org.xendan.logmonitor.web.dao.ConfigurationDaoImpl;

import com.google.inject.servlet.ServletModule;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;

/**
 * @author xendan
 * @since 4.2.14  
 */
public class BindJerseyResources extends ServletModule {
	@Override
    protected void configureServlets() {
        // excplictly bind GuiceContainer before binding Jersey resources
        // otherwise resource won't be available for GuiceContainer
        // when using two-phased injection
        bind(GuiceContainer.class);
        bind(HomeResolver.class);
        bind(ConfigurationDao.class).to(ConfigurationDaoImpl.class);
        // bind Jersey resources
        PackagesResourceConfig resourceConfig = new PackagesResourceConfig("org.xendan.logmonitor.web.service");
        for (Class<?> resource : resourceConfig.getClasses()) {
            bind(resource);
        }

        // Serve resources with Jerseys GuiceContainer
        serve("/*").with(GuiceContainer.class);
    }
}
