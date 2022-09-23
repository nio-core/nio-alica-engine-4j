package de.uniks.vs.jalica.engine.authority;

import de.uniks.vs.jalica.engine.AlicaEngine;
import de.uniks.vs.jalica.engine.AlicaTime;
import de.uniks.vs.jalica.engine.containers.messages.AllocationAuthorityInfo;
import de.uniks.vs.jalica.engine.Assignment;
import de.uniks.vs.jalica.engine.RunningPlan;
import de.uniks.vs.jalica.common.utils.CommonUtils;
import de.uniks.vs.jalica.engine.containers.EntryPointAgents;
import de.uniks.vs.jalica.engine.idmanagement.ID;

import java.util.Vector;
import java.util.concurrent.locks.Lock;

/**
 * Created by alex on 13.07.17.
 * Updated 24.6.19
 */
public class AuthorityManager {

    Vector<AllocationAuthorityInfo> queue = new Vector<>();
    AlicaEngine engine;
    ID localAgentID;
    Lock mutex;

    public AuthorityManager(AlicaEngine ae) {
        this.engine = ae;
        this.localAgentID = null;
    }

    public void init() {
        this.localAgentID = this.engine.getTeamManager().getLocalAgentID();
    }

    public void handleIncomingAuthorityMessage(AllocationAuthorityInfo aai) {
        System.out.println("AM: handleIncomingAuthorityMessage ----------------");
        AlicaTime now = this.engine.getAlicaClock().now();
        if (this.engine.getTeamManager().isAgentIgnored(aai.senderID)) {
            return;
        }
        if ((aai.senderID) != this.localAgentID) {
            this.engine.getTeamManager().setTimeLastMsgReceived(aai.senderID, now);
            if ((aai.senderID.asLong()) > this.localAgentID.asLong()) {
                // notify TO that evidence about other robots is available
                for (EntryPointAgents epr : aai.entryPointAgents) {
                    for (ID rid : epr.agents) {
                        if (rid != this.localAgentID) {
                            this.engine.getTeamManager().setTimeLastMsgReceived(rid, now);
                        }
                    }
                }
            }
        }
        System.out.println("AM: Received AAI Assignment: " + aai);
        {
//            std::lock_guard<std::mutex> lock(this.mutex);
            synchronized (this.queue) {
                this.queue.add(aai);
            }
        }
    }

    public void tick(RunningPlan rp) {
        System.out.println("AM: Tick called! <<<<<<");
//        std::lock_guard<std::mutex> lock(this.mutex);
        synchronized (this.queue) {
            if (rp != null) {
                processPlan(rp);
            }
            this.queue.clear();
        }
    }

    void processPlan(RunningPlan rp) {
        System.out.println("AM: processPlan");

        if (rp.isBehaviour()) {
            return;
        }
        if (rp.getCycleManagement().needsSending()) {
            System.out.println("AM: AAI sended");
            sendAllocation(rp);
            rp.getCycleManagement().sent();
        }

        System.out.println("AM: Queue size of AuthorityInfos is " + this.queue.size());

        for (int i = 0; i < this.queue.size(); i++) {
            if (authorityMatchesPlan(this.queue.get(i), rp)) {
                System.out.println("AM: Found AuthorityInfo, which matches the plan " + rp.getActivePlan().getName());
                rp.getCycleManagement().handleAuthorityInfo(this.queue.get(i));
                this.queue.remove(this.queue.get(i));
                i--;
            }
        }
        for (RunningPlan c : rp.getChildren()) {
            processPlan(c);
        }
    }
    
    void sendAllocation(RunningPlan p)
    {
        if (!this.engine.maySendMessages()) {
            return;
        }
        AllocationAuthorityInfo aai = new AllocationAuthorityInfo();

    Assignment ass = p.getAssignment();
        for (int i = 0; i < ass.getEntryPointCount(); ++i) {
            EntryPointAgents epRobots = new EntryPointAgents();
            epRobots.entrypoint = ass.getEntryPoint(i).getID();
            ass.getAgentsWorking(i, epRobots.agents);

            aai.entryPointAgents.add(epRobots);
        }

        RunningPlan parent = p.getParent();
        aai.parentState = ((parent != null && parent.getActiveState() != null) ? parent.getActiveState().getID() : -1);
        aai.planID = p.getActivePlan().getID();
        aai.authority = this.localAgentID;
        aai.senderID = this.localAgentID;
        aai.planType = (p.getPlanType() != null ? p.getPlanType().getID() : -1);

        System.out.println("AM: Sending AAI Assignment: " + aai);
        this.engine.getCommunicator().sendAllocationAuthority(aai);
    }

    boolean authorityMatchesPlan(AllocationAuthorityInfo aai, RunningPlan p)
    {
        assert(!p.isRetired());
        // If a plan is not retired and does not have a parent, it must be masterplan
        if (p.isRetired()) {
            return false;
        }
    RunningPlan parent = p.getParent();
        if ((parent == null && aai.parentState == -1) ||
                (parent != null && parent.getActiveState() != null && parent.getActiveState().getID() == aai.parentState)) {
        if (p.getActivePlan().getID() == aai.planID) {
            return true;
        } else if (aai.planType != -1 && p.getPlanType() != null && p.getPlanType().getID() == aai.planType) {
            return true;
        }
    }
        return false;
    }
    public void close() {}
}
