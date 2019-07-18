package de.uniks.vs.jalica.engine;

import de.uniks.vs.jalica.common.utils.CommonUtils;
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
 * 23.6.19
 */
public abstract class IAlicaCommunication {

    protected AlicaEngine ae;

    public IAlicaCommunication(AlicaEngine ae) {
        this.ae = ae;
    }

    abstract public void sendAlicaEngineInfo(AlicaEngineInfo statusMessage);
    abstract public void sendRoleSwitch(RoleSwitch rs);
    abstract public void sendAllocationAuthority(AllocationAuthorityInfo aai);
    abstract public void sendPlanTreeInfo(PlanTreeInfo pti);
    abstract public void sendSyncReady(SyncReady sr);
    abstract public void sendSyncTalk(SyncTalk st);
    abstract public void sendSolverResult(SolverResult sr);
    abstract public boolean init();

    abstract public void startCommunication();
    abstract public void stopCommunication();

    protected void onSyncTalkReceived(SyncTalk st) {
        if (ae == null) {
            // case for testing without engine
            System.out.println("\033[93mRecieving ST: " + st.senderID + ' ' + st.syncData.size() + "\033[0m\n");
        } else {
            ae.getSyncModul().onSyncTalk(st);
        }
    }

    protected void onSyncReadyReceived(SyncReady sr) {
        if (ae == null) {
            // case for testing without engine
            System.out.println("\033[93mRecieving SR: " + sr.senderID + ' ' + sr.synchronisationID + "\033[0m\n");
        } else {
            ae.getSyncModul().onSyncReady(sr);
        }
    }

    protected void onAuthorityInfoReceived(AllocationAuthorityInfo aai) {
        if (ae == null) {
            // case for testing without engine
            System.out.println("\033[93mRecieving AAI: " + aai.senderID + ' ' + aai.authority + ' ' + aai.parentState +
                    ' ' + aai.planType + ' ' + aai.planID +"\033[0m\n");
        } else {
            ae.getAuth().handleIncomingAuthorityMessage(aai);
        }
    }

    protected void onPlanTreeInfoReceived(PlanTreeInfo pti) {
        if (ae == null) {
            // case for testing without engine
            System.out.println("\033[93mRecieving PTI: " + pti.senderID + ' ' + pti.succeededEPs.size() + ' ' + pti.stateIDs.size() + "\033[0m\n");
        } else {
            ae.getTeamObserver().handlePlanTreeInfo(pti);
        }
    }

    protected void onSolverResult(SolverResult sr) {
        if (ae == null) {
            // case for testing without engine
            System.out.println("\033[93mRecieving SR: " + sr.senderID + ' ' + sr.vars.size() + "\033[0m\n");
        } else{
            ae.getResultStore().onSolverResult(sr);
        }
    }

    public void tick() { CommonUtils.aboutNoImpl(); };

    protected void sendLogMessage(int level, String message) { CommonUtils.aboutImplIncomplete(level +"  "+ message); };

    public AlicaEngine getAe() {
        return ae;
    }


}
