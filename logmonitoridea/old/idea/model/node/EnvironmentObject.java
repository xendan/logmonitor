package org.xendan.logmonitor.idea.model.node;

import org.xendan.logmonitor.model.Environment;
import org.xendan.logmonitor.model.Server;

/**
* User: id967161
* Date: 27/11/13
*/
public class EnvironmentObject extends EntityObject<Environment> implements ConsoleDisplayable {

    private final Environment entity;

    public EnvironmentObject(Environment entity) {
        super(entity);
        this.entity = entity;
    }

    @Override
    public String toConsoleString() {
        return entity.getName() + ", " + getServerStr(entity.getServer()) + "\n Next update:" + entity.getNextUpdate();
    }

    private String getServerStr(Server server) {
        if (server == null) {
            return Server.LOCALHOST;
        }
        return server.getLogin() + "@" + server.getHost();
    }
}
