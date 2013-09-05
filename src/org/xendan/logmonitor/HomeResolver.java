package org.xendan.logmonitor;

import java.io.File;

/**
 * User: id967161
 * Date: 04/09/13
 */
public class HomeResolver {
    private File home;

    public String getPath(String fileName) {
        return join(getHome().getAbsolutePath(), fileName);
    }

    private File getHome() {
        if (home == null) {
            home = new File(join(System.getProperty("user.home"), ".logmonitor"));
            if (!home.exists() && !home.mkdir()) {
                //TODO log
            }
        }
        return home;
    }

    private String join(String parent, String child) {
        return parent + File.separator + child;
    }

}
