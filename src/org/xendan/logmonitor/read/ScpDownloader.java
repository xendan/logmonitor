package org.xendan.logmonitor.read;

/**
 * User: kcyxa
 * Date: 1/26/13
 */
public class ScpDownloader {

    private String host;
    private String login;
    private String password;
    private String path;

    public ScpDownloader(String host, String login, String password, String path) {
        this.host = host;
        this.login = login;
        this.password = password;
        this.path = path;
    }

//    private static String buildPath() {
//        return System.getProperty("user.home") + "/.logmonitor/tmp.log";
//    }

    public String downloadToLocal() {
        return null;
    }
}
