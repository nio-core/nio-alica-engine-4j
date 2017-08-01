package de.uniks.vs.jalica.unknown;

import de.uniks.vs.jalica.engine.AlicaEngine;

/**
 * Created by alex on 13.07.17.
 */
public class SyncModul implements ISyncModul {
    public SyncModul(AlicaEngine alicaEngine) {
    }

    public void init() {

    }

    @Override
    public void tick() {

    }

    @Override
    public boolean followSyncTransition(Transition t) {
        return false;
    }

    @Override
    public void setSynchronisation(Transition t, boolean b) {

    }
}
