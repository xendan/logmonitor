package org.xendan.logmonitor.web.service;

import com.google.inject.Inject;
import org.apache.log4j.Level;
import org.xendan.logmonitor.model.Configuration;
import org.xendan.logmonitor.model.Environment;
import org.xendan.logmonitor.model.MatchConfig;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Path("/rest/configs")
public class ConfigResource {

    private final LogService service;

    @Inject
    public ConfigResource(LogService service) {
        this.service = service;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Configuration> getAllConfigs() {
        return service.getConfigs();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{configId}")
    public Configuration getConfig(@PathParam("configId") Long configId, @DefaultValue("") @QueryParam("projectName") String projectName) {
        if (configId == -1) {
            Configuration configuration = new Configuration();
            configuration.setId(-1L);
            configuration.setEnvironments(Arrays.asList(createDevEnv()));
            configuration.setProjectName(projectName);
            return configuration;
        }
        return service.getConfig(configId);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public void saveConfig(Configuration configuration) {
        service.merge(configuration);
    }

    private Environment createDevEnv() {
        Environment dev = new Environment();
        dev.setId(-1L);
        dev.setName("DEV");
        dev.setMatchConfigs(createAnyErrorMatchConfig());
        dev.setUpdateInterval(5);
        return dev;
    }

    private List<MatchConfig> createAnyErrorMatchConfig() {
        List<MatchConfig> matchers = new ArrayList<MatchConfig>();
        matchers.add(createError());
        return matchers;
    }

    private MatchConfig createError() {
        MatchConfig match = new MatchConfig();
        match.setId(-1L);
        match.setName("General error");
        match.setLevel(Level.ERROR.toString());
        match.setShowNotification(true);
        match.setGeneral(true);
        return match;
    }
}
