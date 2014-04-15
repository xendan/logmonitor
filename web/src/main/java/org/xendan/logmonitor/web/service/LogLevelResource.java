package org.xendan.logmonitor.web.service;

import org.apache.log4j.Level;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

@Path("/rest/loglevels")
public class LogLevelResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> getAllLevels() {
        Level[] levels = {Level.FATAL, Level.ERROR, Level.WARN, Level.INFO, Level.DEBUG, Level.TRACE, Level.ALL};
        List<String> levelNames = new ArrayList<String>(levels.length);
        for (Level level : levels) {
            levelNames.add(level.toString());
        }
        return levelNames;
    }
}
