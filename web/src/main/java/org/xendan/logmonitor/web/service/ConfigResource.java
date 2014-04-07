package org.xendan.logmonitor.web.service;

import com.google.inject.Inject;
import org.apache.log4j.Level;
import org.xendan.logmonitor.model.Configuration;
import org.xendan.logmonitor.model.Environment;
import org.xendan.logmonitor.model.MatchConfig;
import org.xendan.logmonitor.web.dao.ConfigurationDao;
import org.xendan.logmonitor.web.model.Configurations;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Path("/rest/configs")
public class ConfigResource {

    private final ConfigurationDao dao;


    @Inject
    public ConfigResource(ConfigurationDao dao) {
        this.dao = dao;
    }


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public org.xendan.logmonitor.web.model.Configurations getAllConfigs() {
        Configurations configs = new Configurations();
        configs.setConfigurations(dao.getConfigs());
        return configs;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{configId}")
    public Configuration getConfig(@PathParam("configId") Integer configId) {
        if (configId == -1) {
            Configuration configuration = new Configuration();
            configuration.setEnvironments(Arrays.asList(createDevEnv()));
            return configuration;
        }
        return null;
    }

    private Environment createDevEnv() {
        Environment dev = new Environment();
        dev.setName("DEV");
        dev.setMatchConfigs(createAnyErrorMatchConfig());
        return dev;
    }

    private List<MatchConfig> createAnyErrorMatchConfig() {
        List<MatchConfig> matchers = new ArrayList<MatchConfig>();
        matchers.add(createError());
        return matchers;
    }

    private MatchConfig createError() {
        MatchConfig match = new MatchConfig();
        match.setLevel(Level.ERROR.toString());
        return match;
    }

}
