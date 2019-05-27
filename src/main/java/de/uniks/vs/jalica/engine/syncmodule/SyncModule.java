package de.uniks.vs.jalica.engine.syncmodule;

import de.uniks.vs.jalica.engine.AlicaCommunication;
import de.uniks.vs.jalica.engine.AlicaEngine;
import de.uniks.vs.jalica.engine.PlanRepository;
import de.uniks.vs.jalica.engine.model.Synchronisation;
import de.uniks.vs.jalica.engine.model.Transition;
import de.uniks.vs.jalica.engine.containers.SyncReady;
import de.uniks.vs.jalica.engine.containers.SyncTalk;
import de.uniks.vs.jalica.engine.common.CommonUtils;
import de.uniks.vs.jalica.engine.common.SyncTransition;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by alex on 13.07.17.
 */
public class SyncModule implements ISyncModule {
    private AlicaEngine ae;
    private boolean running;
    private long myId;
    private long ticks;
    private PlanRepository pr;
    private AlicaCommunication communicator;
    private HashMap<SyncTransition, Synchronisation> synchSet = new HashMap<>();
    private ArrayList<SyncTransition> synchedTransitions = new ArrayList<>();

    public SyncModule(AlicaEngine ae) {
        this.ae = ae;
    }

    public void init() {
        this.ticks = 0;
        this.running = true;
        this.myId = ae.getTeamObserver().getOwnID();
        this.pr = this.ae.getPlanRepository();
        this.communicator = this.ae.getCommunicator();
    }

    @Override
    public void tick() {
        ArrayList<Synchronisation> failedSyncs = new ArrayList<>();
//        lock_guard<mutex> lock(lomutex);
        for (Synchronisation synchronisation : this.synchSet.values()) {

            if (!synchronisation.isValid(ticks)) {
                failedSyncs.add(synchronisation);
            }
            ticks++;
            for (Synchronisation s : failedSyncs) {
                this.synchSet.remove(s.getSyncTransition());
            }
        }
    }

    @Override
    public boolean followSyncTransition(Transition t) {
        CommonUtils.aboutNoImpl(); return false;
    }

    @Override
    public void setSynchronisation(Transition t, boolean b) {
        CommonUtils.aboutNoImpl();
    }

    @Override
    public void onSyncTalk(SyncTalk st) {
        CommonUtils.aboutNoImpl();
    }

    @Override
    public void onSyncReady(SyncReady sr) {
        CommonUtils.aboutNoImpl();
    }

    public void sendSyncTalk(SyncTalk st) {

        if (!this.ae.isMaySendMessages())
            return;
        st.senderID = this.myId;
        this.communicator.sendSyncTalk(st);

    }
}
