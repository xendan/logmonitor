package org.xendan.logmonitor.web.service.resources;

import com.google.inject.Inject;
import org.xendan.logmonitor.model.EnvironmentStatus;
import org.xendan.logmonitor.web.service.EnvironmentMonitor;
import org.xendan.logmonitor.web.service.LogService;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/rest/logentries")
public class LogEntryResource {

    private LogService service;
    private EnvironmentMonitor monitor;

    @Inject
    public LogEntryResource(LogService service, EnvironmentMonitor monitor) {
        this.service = service;
        this.monitor = monitor;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/envstatus/{envId}")
    public EnvironmentStatus getStatus(@PathParam("envId") Long envId) {
        return monitor.getStatus(envId);
    }
}
