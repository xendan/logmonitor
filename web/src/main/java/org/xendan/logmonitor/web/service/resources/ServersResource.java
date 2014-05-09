package org.xendan.logmonitor.web.service.resources;

import com.google.inject.Inject;
import org.xendan.logmonitor.model.Server;
import org.xendan.logmonitor.web.dao.ConfigurationDao;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/rest/servers")
public class ServersResource {
    private final ConfigurationDao dao;


    @Inject
    public ServersResource(ConfigurationDao dao) {
        this.dao = dao;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Server> getAllServers() {
       return dao.getAllServers();
    }
}
