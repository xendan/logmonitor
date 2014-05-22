package org.xendan.logmonitor.idea.read;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.util.containers.HashSet;

import java.io.Serializable;
import java.util.Set;

/**
 * @author xendan
 * @since 4/20/14.
 */
@State(
        name = "Logmonitorsettings",
        storages = {
                @Storage(
                        file = StoragePathMacros.APP_CONFIG + "/logmonitorsettings.xml")
        }
)
public class Settings implements PersistentStateComponent<Settings.State> {

    private State myState = new State();

    @Override
    public State getState() {
        return myState;
    }

    @Override
    public void loadState(State state) {
        myState = state;
    }

    public void reset(State initialState) {
        myState.setUseBuiltInServer(initialState.useBuiltInServer);
        myState.setIgnoredEnvironments(initialState.getIgnoredEnvironments());
        myState.setPort(initialState.getPort());
        myState.setUrl(initialState.getUrl());
    }

    public static class State implements Serializable {
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

            State state = (State) o;

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
}
