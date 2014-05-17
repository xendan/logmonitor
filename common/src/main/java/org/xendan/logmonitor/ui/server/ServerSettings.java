package org.xendan.logmonitor.ui.server;

import java.util.HashSet;

import java.io.Serializable;
import java.util.Set;

/**
* @author ksenia
* @since 13.05.14.
*/
public class ServerSettings implements Serializable {
    private Boolean useBuiltInServer;
    private int port = 8085;
    private String url;
    private Set<Integer> ignoredEnvironments = new HashSet<Integer>();

    public Boolean getUseBuiltInServer() {
        return useBuiltInServer;
    }

    public void setUseBuiltInServer(Boolean useBuiltInServer) {
        this.useBuiltInServer = useBuiltInServer;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Set<Integer> getIgnoredEnvironments() {
        return ignoredEnvironments;
    }

    public void setIgnoredEnvironments(Set<Integer> ignoredEnvironments) {
        this.ignoredEnvironments = ignoredEnvironments;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ServerSettings state = (ServerSettings) o;

        if (port != state.port) return false;
        if (ignoredEnvironments != null ? !ignoredEnvironments.equals(state.ignoredEnvironments) : state.ignoredEnvironments != null)
            return false;
        if (url != null ? !url.equals(state.url) : state.url != null) return false;
        return !(useBuiltInServer != null ? !useBuiltInServer.equals(state.useBuiltInServer) : state.useBuiltInServer != null);

    }

    @Override
    public int hashCode() {
        int result = useBuiltInServer != null ? useBuiltInServer.hashCode() : 0;
        result = 31 * result + port;
        result = 31 * result + (url != null ? url.hashCode() : 0);
        result = 31 * result + (ignoredEnvironments != null ? ignoredEnvironments.hashCode() : 0);
        return result;
    }
}
