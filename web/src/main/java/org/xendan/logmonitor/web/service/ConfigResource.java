package org.xendan.logmonitor.web.service;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.xendan.logmonitor.web.dao.ConfigurationDao;

import com.google.inject.Inject;

@Path("/rest/configs")
public class ConfigResource {
	
	private final ConfigurationDao dao;


	@Inject
	public ConfigResource(ConfigurationDao dao) {
		this.dao = dao;
	}
	
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String getAllConfigs() {
		System.err.println("DAO| IS " + dao);
		return "[{project:'aaa'}, {project:'bbb'}]";
	}

}
