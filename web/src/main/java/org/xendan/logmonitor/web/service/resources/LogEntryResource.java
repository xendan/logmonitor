package org.xendan.logmonitor.web.service.resources;

import com.google.inject.Inject;
import org.xendan.logmonitor.model.EnvironmentStatus;
import org.xendan.logmonitor.web.service.EnvironmentMonitor;
import org.xendan.logmonitor.web.service.LogService;

import javax.ws.rs.*;
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

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public EnvironmentStatus getEntries(@QueryParam("envId") Long envId, @QueryParam("matcherId") Long matcherId, @QueryParam("isGeneral") boolean isGeneral) {
        return monitor.getStatus(envId);
    }
}
