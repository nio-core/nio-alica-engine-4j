package de.uniks.vs.jalica.dummy_proxy;

import de.uniks.vs.jalica.engine.AlicaEngine;
import de.uniks.vs.jalica.unknown.*;

/**
 * Created by alex on 13.07.17.
 */
public class AlicaDummyCommunication extends AlicaCommunication {

    private AlicaEngine ae;

    public AlicaDummyCommunication(AlicaEngine ae) {
        this.ae = ae;
    }

    @Override
    public void startCommunication() { CommonUtils.aboutNoImpl(); }

    @Override
    public void stopCommunication() {
        CommonUtils.aboutNoImpl();
    }

    @Override
    public void tick() {
        CommonUtils.aboutNoImpl();
    }

    @Override
    public void sendAlicaEngineInfo(AlicaEngineInfo statusMessage) {
        CommonUtils.aboutNoImpl();
    }

    @Override
    public void sendRoleSwitch(RoleSwitch rs) {
        CommonUtils.aboutNoImpl();
    }

    @Override
    public void sendAllocationAuthority(AllocationAuthorityInfo aai) {
        CommonUtils.aboutNoImpl();
    }

    @Override
    public void sendPlanTreeInfo(PlanTreeInfo pti) {CommonUtils.aboutNoImpl();}

    @Override
    public void sendSyncReady(SyncReady sr) {
        CommonUtils.aboutNoImpl();
    }

    @Override
    public void sendSyncTalk(SyncTalk st) {
        CommonUtils.aboutNoImpl();
    }

    @Override
    public void sendSolverResult(SolverResult sr) {
        CommonUtils.aboutNoImpl();
    }
}
