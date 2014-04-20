package org.xendan.logmonitor.idea;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import org.jetbrains.annotations.Nullable;

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

    public static class State {

    }
}
