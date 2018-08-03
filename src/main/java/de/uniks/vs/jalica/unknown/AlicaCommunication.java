package de.uniks.vs.jalica.unknown;

import de.uniks.vs.jalica.engine.AlicaEngine;

/**
 * Created by alex on 13.07.17.
 */
public abstract class AlicaCommunication {

    protected AlicaEngine ae;

    public AlicaCommunication(AlicaEngine ae) {
        this.ae = ae;
    }

    public abstract void sendAlicaEngineInfo(AlicaEngineInfo statusMessage);
    public abstract void sendRoleSwitch(RoleSwitch rs);
    public abstract void sendAllocationAuthority(AllocationAuthorityInfo aai);
    public abstract void sendPlanTreeInfo(PlanTreeInfo pti);
    public abstract void sendSyncReady(SyncReady sr);
    public abstract void sendSyncTalk(SyncTalk st);
    public abstract void sendSolverResult(SolverResult sr);

    abstract public void startCommunication();
    abstract public void stopCommunication();

    void sendLogMessage(int level, String message) { CommonUtils.aboutNoImpl(); };

    public void tick() { CommonUtils.aboutNoImpl(); };

    protected void onSyncTalkReceived(SyncTalk st) {
        ae.getSyncModul().onSyncTalk(st);
    }

    protected void onSyncReadyReceived(SyncReady sr) {
        ae.getSyncModul().onSyncReady(sr);
    }

    protected void onAuthorityInfoReceived(AllocationAuthorityInfo aai) {
        ae.getAuth().handleIncomingAuthorityMessage(aai);
    }

    protected void onPlanTreeInfoReceived(PlanTreeInfo pti) {
        ae.getTeamObserver().handlePlanTreeInfo(pti);
    }

    protected void onSolverResult(SolverResult sr) {
        ae.getResultStore().onSolverResult(sr);
    }

    public abstract boolean init();
}
