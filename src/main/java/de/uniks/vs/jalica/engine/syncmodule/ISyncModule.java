package de.uniks.vs.jalica.engine.syncmodule;

import de.uniks.vs.jalica.engine.model.Transition;
import de.uniks.vs.jalica.engine.containers.SyncReady;
import de.uniks.vs.jalica.engine.containers.SyncTalk;

/**
 * Created by alex on 13.07.17.
 */
public interface ISyncModule {
    void tick();

    boolean followSyncTransition(Transition t);

    void setSynchronisation(Transition t, boolean b);

    void onSyncTalk(SyncTalk st);

    void onSyncReady(SyncReady sr);
}
