package org.xendan.logmonitor.read.command;

import org.apache.commons.lang.StringUtils;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.optional.ssh.SSHBase;
import org.xendan.logmonitor.model.Server;

/**
 * User: id967161
 * Date: 13/09/13
 */
public class BaseCommand {
    protected final Server settings;

    public BaseCommand(Server settings) {
        this.settings = settings;
    }

    protected <T extends SSHBase> T initTask(T task) {
        task.setProject(new Project()); // prevent a NPE (Ant works with projects)
        task.setTrust(true); // workaround for not supplying known hosts file
        task.setPassword(settings.getPassword());
        task.setHost(settings.getHost());
        task.setUsername(settings.getLogin());
        return task;
    }

    public String getConnectionStr() {
        if (StringUtils.isEmpty(settings.getLogin())) {
            return settings.getHost();
        }
        return settings.getLogin() + "@" + settings.getHost();
    }
}
