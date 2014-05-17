package org.xendan.logmonitor.web.service.resources;

import com.google.inject.Inject;
import org.xendan.logmonitor.model.EntriesList;
import org.xendan.logmonitor.model.EnvironmentStatus;
import org.xendan.logmonitor.model.LogEntry;
import org.xendan.logmonitor.model.LogEntryGroup;
import org.xendan.logmonitor.web.read.parse.PatternUtils;
import org.xendan.logmonitor.web.service.EnvironmentMonitor;
import org.xendan.logmonitor.web.service.LogService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Collections;
import java.util.List;

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
    public EntriesList getEntries(@QueryParam("envId") Long envId, @QueryParam("matcherId") Long matcherId, @QueryParam("isGeneral") boolean isGeneral) {
        List<LogEntryGroup> groups = isGeneral ? service.getMatchedEntryGroups(matcherId, envId) : Collections.<LogEntryGroup>emptyList();
        List<LogEntry> notGrouped = service.getNotGroupedMatchedEntries(matcherId, envId);
        deleteEnvironments(notGrouped);
        for (LogEntryGroup group : groups) {
            deleteEnvironments(group.getEntries());
            for (LogEntry entry : group.getEntries()) {
                entry.setExpandedMessage(PatternUtils.restoreMessage(entry, group.getMessagePattern()));
            }
        }
        return new EntriesList(groups, notGrouped);
    }

    private void deleteEnvironments(List<LogEntry> entries) {
        for (LogEntry entry : entries) {
            entry.setMatchConfig(null);
            entry.setEnvironment(null);
        }

    }
}
