package org.xendan.logmonitor.read.command;

import org.apache.commons.lang.StringUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.optional.ssh.SSHExec;
import org.xendan.logmonitor.model.Server;
import org.xendan.logmonitor.read.command.BaseCommand;

import java.util.ArrayList;
import java.util.List;

/**
 * User: id967161
 * Date: 13/09/13
 */
public class LsCommand extends BaseCommand {
    public LsCommand(Server settings) {
        super(settings);
    }

    public String[][] getDirsAndFiles(String path) {
        String[][] dirsAndFiles = new String[2][];
        try {
            dirsAndFiles[0] = getList(path, "d");
            dirsAndFiles[1] = getList(path, "f");
            return dirsAndFiles;
        } catch (BuildException e) {
            return null;
        }
    }

    private String[] getList(String path, String type) {
        SSHExec ls = initTask(new SSHExec());
        ls.setCommand("find " + path + " -maxdepth 1 -type " + type);
        ls.setOutputproperty("output");
        ls.execute();
        return parse(ls.getProject().getProperty("output"), path);
    }


    public String[] parse(String lsOutput, String path) {
        List<String> elem = new ArrayList<String>();
        if (StringUtils.isNotEmpty(lsOutput)) {
            for (String str : lsOutput.split("\n")) {
                if (!path.equals(str)) {
                    elem.add(str.replace(path.equals("/") ? path : path + "/", ""));
                }
            }
        }
        return elem.toArray(new String[elem.size()]);
    }
}
