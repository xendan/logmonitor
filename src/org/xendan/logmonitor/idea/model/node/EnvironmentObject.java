package org.xendan.logmonitor.idea.model.node;

import org.joda.time.LocalDateTime;
import org.xendan.logmonitor.idea.model.LogMonitorPanelModel;
import org.xendan.logmonitor.model.Environment;
import org.xendan.logmonitor.model.Server;

import java.util.Map;

/**
* User: id967161
* Date: 27/11/13
*/
public class EnvironmentObject extends EntityObject<Environment> implements ConsoleDisplayable {

    private final Environment entity;
    private final Map<Environment, LocalDateTime> nextUpdate;

    public EnvironmentObject(Environment entity, Map<Environment, LocalDateTime> nextUpdate) {
        super(entity);
        this.entity = entity;
        this.nextUpdate = nextUpdate;
    }

    @Override
    public String toConsoleString() {
        return entity.getName() + ", " + getServerStr(entity.getServer()) + "\n Next update:" + getUpdate(nextUpdate.get(entity));
    }

    private String getUpdate(LocalDateTime time) {
        if (time == null) {
            return "soon";
        }
        return LogMonitorPanelModel.HOURS_MINUTES.print(time);
    }

    private String getServerStr(Server server) {
        if (server == null) {
            return Server.LOCALHOST;
        }
        return server.getLogin() + "@" + server.getHost();
    }
}
