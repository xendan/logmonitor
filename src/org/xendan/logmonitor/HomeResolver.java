package org.xendan.logmonitor;

import java.io.File;

/**
 * User: id967161
 * Date: 04/09/13
 */
public class HomeResolver {

    public  static final String HOME = ".logmonitor";
    private File home;

    public String getPath(String localPath) {
        return join(getHome().getAbsolutePath(), localPath);
    }

    private File getHome() {
        if (home == null) {
            home = mkdirs(join(System.getProperty("user.home"), HOME));
        }
        return home;
    }

    private File mkdirs(String path) {
        File file = new File(path, File.separator);
        if (!file.exists() && !file.mkdirs()) {
            logErroDir(file);
        }
        return file;
    }

    private void logErroDir(File file) {
        log("Error creating " + file.getAbsolutePath());
    }

    private void log(String message) {
        //TODO log
    }

    public String join(String parent, String child) {
        return parent + File.separator + child;
    }

    public String joinMkDirs(String file, String... dirs) {
        String fullDirs = getHome().getAbsolutePath();
        for (String dir : dirs) {
            fullDirs = join(fullDirs, dir);
        }
        File dirsFile = mkdirs(fullDirs);
        return join(fullDirs, file);
    }


}
