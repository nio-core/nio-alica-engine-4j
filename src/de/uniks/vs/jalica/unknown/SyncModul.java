package de.uniks.vs.jalica.unknown;

import de.uniks.vs.jalica.engine.AlicaEngine;
import de.uniks.vs.jalica.teamobserver.PlanRepository;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by alex on 13.07.17.
 */
public class SyncModul implements ISyncModul {
    private AlicaEngine ae;
    private boolean running;
    private int myId;
    private long ticks;
    private PlanRepository pr;
    private AlicaCommunication communicator;
    private HashMap<SyncTransition, Synchronisation> synchSet = new HashMap<>();
    private ArrayList<SyncTransition> synchedTransitions = new ArrayList<>();

    public SyncModul(AlicaEngine ae) {
        this.ae = ae;
    }

    public void init() {
        this.ticks = 0;
        this.running = true;
        this.myId = ae.getTeamObserver().getOwnId();
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
