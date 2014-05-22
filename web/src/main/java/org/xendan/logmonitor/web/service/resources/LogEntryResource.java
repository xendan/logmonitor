package org.xendan.logmonitor.web.service.resources;

import com.google.inject.Inject;
import org.joda.time.LocalDateTime;
import org.xendan.logmonitor.model.EntriesList;
import org.xendan.logmonitor.model.EnvironmentStatus;
import org.xendan.logmonitor.model.LogEntry;
import org.xendan.logmonitor.model.LogEntryGroup;
import org.xendan.logmonitor.web.read.parse.PatternUtils;
import org.xendan.logmonitor.web.read.schedule.ReaderScheduler;
import org.xendan.logmonitor.web.service.EnvironmentMonitor;
import org.xendan.logmonitor.web.service.LogService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Path("/rest/logentries")
public class LogEntryResource {

    private LogService service;
    private EnvironmentMonitor monitor;
    private ReaderScheduler scheduler;

    @Inject
    public LogEntryResource(LogService service, EnvironmentMonitor monitor, ReaderScheduler scheduler) {
        this.service = service;
        this.monitor = monitor;
        this.scheduler = scheduler;
    }

    @DELETE
    @Path("/env/{envId}")
    public void deleteEntriesInEnvironment(@PathParam("envId") Long envId, @QueryParam("mathcerIds") List<Long> matcherId) {
        service.removeAllEntries(envId, matcherId);
    }


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/envstatus/{envId}")
    public EnvironmentStatus getStatus(@PathParam("envId") Long envId) {
        return monitor.getStatus(envId);
    }


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public EntriesList getEntries(
            @QueryParam("envId") Long envId,
            @QueryParam("matcherId") Long matcherId,
            @QueryParam("isGeneral") boolean isGeneral,
            @QueryParam("since") Long sinceMillis,
            @QueryParam("refresh") boolean refresh
    ) {
        if (refresh) {
            scheduler.refreshSynchronously(envId);
        }
        LocalDateTime since = LocalDateTime.fromDateFields(new Date(sinceMillis));
        List<LogEntryGroup> groups = isGeneral ? service.getEntryGroups(matcherId, envId, since) : Collections.<LogEntryGroup>emptyList();
        List<LogEntry> notGrouped = service.getNotGroupedEntries(matcherId, envId, since);
        removeEnvironments(notGrouped);
        for (LogEntryGroup group : groups) {
            removeEnvironments(group.getEntries());
            for (LogEntry entry : group.getEntries()) {
                entry.setExpandedMessage(PatternUtils.restoreMessage(entry, group.getMessagePattern()));
            }
        }
        return new EntriesList(groups, notGrouped);
    }

    private void removeEnvironments(List<LogEntry> entries) {
        for (LogEntry entry : entries) {
            entry.setMatchConfig(null);
            entry.setEnvironment(null);
        }

    }
}
