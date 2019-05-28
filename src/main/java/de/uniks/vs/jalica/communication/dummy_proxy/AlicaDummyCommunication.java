package de.uniks.vs.jalica.communication.dummy_proxy;

import de.uniks.vs.jalica.common.utils.CommonUtils;
import de.uniks.vs.jalica.engine.AlicaCommunication;
import de.uniks.vs.jalica.engine.AlicaEngine;
import de.uniks.vs.jalica.engine.containers.messages.AlicaEngineInfo;
import de.uniks.vs.jalica.engine.containers.messages.AllocationAuthorityInfo;
import de.uniks.vs.jalica.engine.containers.messages.PlanTreeInfo;
import de.uniks.vs.jalica.engine.containers.RoleSwitch;
import de.uniks.vs.jalica.engine.containers.SolverResult;
import de.uniks.vs.jalica.engine.containers.SyncReady;
import de.uniks.vs.jalica.engine.containers.SyncTalk;

import java.util.ArrayList;

/**
 * Created by alex on 13.07.17.
 */
public class AlicaDummyCommunication extends AlicaCommunication {

    public AlicaDummyCommunication(AlicaEngine ae) { super(ae); }

    @Override
    public void startCommunication() { CommonUtils.aboutNoImpl(); }

    @Override
    public void stopCommunication() {
        CommonUtils.aboutNoImpl();
    }

    @Override
    public void tick() { CommonUtils.aboutNoImpl(); }

    @Override
    public boolean init(ArrayList<Long> ids) { return true; }

    @Override
    public void sendAlicaEngineInfo(AlicaEngineInfo statusMessage) { CommonUtils.aboutNoImpl(); }

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
