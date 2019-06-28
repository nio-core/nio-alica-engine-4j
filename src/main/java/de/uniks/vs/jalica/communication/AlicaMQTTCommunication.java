package de.uniks.vs.jalica.communication;

import de.uniks.vs.jalica.common.utils.CommonUtils;
import de.uniks.vs.jalica.engine.IAlicaCommunication;
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
 * Created by alex on 29.06.18.
 */
public class AlicaMQTTCommunication extends IAlicaCommunication {

    private boolean isRunning;

    public AlicaMQTTCommunication(AlicaEngine ae) {
        super(ae);
        this.isRunning = false;
    }

    @Override
    public boolean init() {
        CommonUtils.aboutImplIncomplete();
        return true;
    }

    @Override
    public void startCommunication() {
        this.isRunning = true;
    }

    @Override
    public void stopCommunication() {
        this.isRunning = false;
    }

    @Override
    public void tick() {

        if (this.isRunning) {
            //Use this for synchronous communication!
        }
    }

    @Override
    public void sendAllocationAuthority(AllocationAuthorityInfo aai) {

        if (this.isRunning){
//            this.AllocationAuthorityInfoPublisher.publish(aai);
        }
    }

    @Override
    public void sendAlicaEngineInfo(AlicaEngineInfo statInfo) {

        if (this.isRunning) {
//            this.AlicaEngineInfoPublisher.publish(statInfo);
        }
    }

    @Override
    public void sendRoleSwitch(RoleSwitch rs) {

        if (this.isRunning){
//            this.RoleSwitchPublisher.publish(rs);
        }
    }

    @Override
    public void sendPlanTreeInfo(PlanTreeInfo pti) {

        if (this.isRunning) {
//            this.PlanTreeInfoPublisher.publish(pti);
        }
    }

    @Override
    public void sendSyncReady(SyncReady sr) {
        if (this.isRunning){
//            this.SyncReadyPublisher.publish(sr);
        }
    }

    @Override
    public void sendSyncTalk(SyncTalk st) {

        if (this.isRunning){
//            this.SyncTalkPublisher.publish(st);
        }
    }

    @Override
    public void sendSolverResult(SolverResult sr) {

        if (this.isRunning) {
//            this.SolverResultPublisher.publish(sr);
        }
    }

    public void handleAllocationAuthorityRos(AllocationAuthorityInfo aai) {

        if (this.isRunning) {
            this.onAuthorityInfoReceived(aai);
        }
    }

    public void handlePlanTreeInfoRos(PlanTreeInfo pti) {

        if (this.isRunning) {
            this.onPlanTreeInfoReceived(pti);
        }
    }

    public void handleSyncReadyRos(SyncReady sr) {

        if (this.isRunning) {
            this.onSyncReadyReceived(sr);
        }
    }

    public void handleSyncTalkRos(SyncTalk st) {
        if (this.isRunning)
        {
            this.onSyncTalkReceived(st);
        }
    }

    public void handleSolverResult(SolverResult sr) {

        if (this.isRunning) {
            this.onSolverResult(sr);
        }
    }
}
