package de.uniks.vs.jalica.unknown;

/**
 * Created by alex on 13.07.17.
 */
public interface ISyncModul {
    void tick();

    boolean followSyncTransition(Transition t);

    void setSynchronisation(Transition t, boolean b);

    void onSyncTalk(SyncTalk st);

    void onSyncReady(SyncReady sr);
}
