package org.xendan.logmonitor.web.service;

import com.google.inject.Inject;
import org.xendan.logmonitor.web.dao.ConfigurationDao;
import org.xendan.logmonitor.web.model.Configurations;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/rest/configs")
public class ConfigResource {
	
	private final ConfigurationDao dao;


	@Inject
	public ConfigResource(ConfigurationDao dao) {
		this.dao = dao;
	}
	
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Configurations getAllConfigs() {
        Configurations configs = new Configurations();
		configs.setConfigurations(dao.getConfigs());
        return configs;
	}

}
