package de.uniks.vs.jalica.unknown;

import de.uniks.vs.jalica.engine.AlicaEngine;

import java.util.Vector;

/**
 * Created by alex on 13.07.17.
 */
public class AuthorityManager {

    Vector<AllocationAuthorityInfo> queue = new Vector<>();
    AlicaEngine ae;
    long ownID;

    public AuthorityManager(AlicaEngine ae) {
        this.ae = ae;
        this.ownID = -1;
    }

    public void init() {
        this.ownID = ae.getTeamObserver().getOwnID();
    }

    public void tick(RunningPlan rp) {
        if (CommonUtils.AM_DEBUG_debug) System.out.println("AM: Tick called!" );
//        lock_guard<mutex> lock(mu);
        processPlan(rp);
        this.queue.clear();
    }

    private void processPlan(RunningPlan rp) {

        if (rp == null || rp.isBehaviour())
            return;

        if (rp.getCycleManagement().needsSending()) {
            sendAllocation(rp);
            rp.getCycleManagement().sent();
        }

        if (CommonUtils.AM_DEBUG_debug) System.out.println("AM: Queue size of AuthorityInfos is " + this.queue.size() );

        for (int i = 0; i < this.queue.size(); i++) {

            if (authorityMatchesPlan(this.queue.get(i), rp)) {

                if (CommonUtils.AM_DEBUG_debug)  System.out.println( "AM: Found AuthorityInfo, which matches the plan " + rp.getPlan().getName() );
                rp.getCycleManagement().handleAuthorityInfo(this.queue.get(i));
                this.queue.remove(this.queue.get(i));
                i--;
            }
        }

        for (RunningPlan c : rp.getChildren()) {
            processPlan(c);
        }

    }

//    private boolean authorityMatchesPlan2(AllocationAuthorityInfo allocationAuthorityInfo, RunningPlan runningPlan) {
////        assert(!p.isRetired());
////        // If a plan is not retired and does not have a parent, it must be masterplan
////        if (p.isRetired()) {
////            return false;
////        }
//        RunningPlan parent = runningPlan.getParent();
//
//        if ((parent == null && allocationAuthorityInfo.parentState == -1) ||
//                (parent != null && parent.getActiveState() != null && parent.getActiveState().getID() == allocationAuthorityInfo.parentState)) {
//
////        if (runningPlan.getActivePlan().getID() == allocationAuthorityInfo.planID) {
//            if (runningPlan.getPlan().getID() == allocationAuthorityInfo.planID) {
//                return true;
//            } else if (allocationAuthorityInfo.planType != -1 && runningPlan.getPlanType() != null && runningPlan.getPlanType().getID() == allocationAuthorityInfo.planType) {
//                return true;
//            }
//        }
//        return false;
//    }

    private boolean authorityMatchesPlan(AllocationAuthorityInfo allocationAuthorityInfo, RunningPlan runningPlan) {
        RunningPlan parent = runningPlan.getParent();
//        auto shared = runningPlan.getParent().lock();

		if (runningPlan.getParent() != null) {
            if (CommonUtils.AM_DEBUG_debug) System.out.println( "AM: Parent-Weak is NOT expired!");
            if (CommonUtils.AM_DEBUG_debug) System.out.println( "AM: Parent-ActiveState is: " + (parent.getActiveState() != null ? parent.getActiveState().getID() : null));
            if (CommonUtils.AM_DEBUG_debug) System.out.println( "AM: AAI-ParentState is: " + allocationAuthorityInfo.parentState);
		}
		else {
            if (CommonUtils.AM_DEBUG_debug) System.out.println(  "AM: Parent-Weak is expired!");
            if (CommonUtils.AM_DEBUG_debug) System.out.println(  "AM: Current-ActiveState is: " + runningPlan.getActiveState().getID());
            if (CommonUtils.AM_DEBUG_debug) System.out.println( "AM: AAI-ParentState is: " + allocationAuthorityInfo.parentState);
		}

        if ((parent == null && allocationAuthorityInfo.parentState == -1)
                || (parent != null && parent.getActiveState() != null
                && parent.getActiveState().getID() == allocationAuthorityInfo.parentState)) {

            if (runningPlan.getPlan().getID() == allocationAuthorityInfo.planID) {
                return true;
            }  else if (allocationAuthorityInfo.planType != -1 && runningPlan.getPlanType() != null && runningPlan.getPlanType().getID() == allocationAuthorityInfo.planType) {
                return true;
            }
        }
        return false;
    }

    public void handleIncomingAuthorityMessage(AllocationAuthorityInfo aai) {

        if (ae.getTeamObserver().isAgentIgnored(aai.senderID)) {
            return;
        }

        if (aai.senderID != this.ownID) {
            ae.getTeamObserver().messageRecievedFrom(aai.senderID);

            if (aai.senderID > this.ownID) {

                //notify TO that evidence about other robots is available
                for (EntryPointAgents epr : aai.entryPointAgents) {

                    for (long rid : epr.agents) {

                        if (rid != this.ownID) {
                            ae.getTeamObserver().messageRecievedFrom(rid);
                        }
                    }
                }
            }
        }

        if (CommonUtils.AM_DEBUG_debug) {
            String ss = "";
            ss += "AM: Received AAI Assignment from " + aai.senderID + " is: ";
            for (EntryPointAgents entryPointAgents : aai.entryPointAgents) {
                ss += "EP: " + entryPointAgents.entrypoint + " Agents: ";
                for (long agentID : entryPointAgents.agents) {
                    ss += agentID + ", ";
                }
                ss += "\n";
            }
            System.out.println(ss);
        }

        {
//                lock_guard<mutex> lock(mu);
            this.queue.add(aai);
        }
    }

    public void close() {CommonUtils.aboutNoImpl();}

    public void sendAllocation(RunningPlan p) {

        if (!this.ae.isMaySendMessages()) {
            return;
        }
        AllocationAuthorityInfo aai = new AllocationAuthorityInfo();
        Assignment ass = p.getAssignment();

        for (int i = 0; i < ass.getEntryPointCount(); i++) {
            EntryPointAgents entryPointAgents = new EntryPointAgents();
            entryPointAgents.entrypoint = ass.getEpAgentsMapping().getEp(i).getID();

            for (long robot : ass.getAgentsWorking(entryPointAgents.entrypoint)) {
                entryPointAgents.agents.add(robot);
            }
            aai.entryPointAgents.add(entryPointAgents);
        }

        RunningPlan shared = p.getParent();
        aai.parentState = ((shared == null || shared.getActiveState() == null) ? -1 : shared.getActiveState().getID());
        aai.planID = p.getPlan().getID();
        aai.authority = this.ownID; // OOOOOOOH
        aai.senderID = this.ownID;
        aai.planType = (p.getPlanType() == null ? -1 : p.getPlanType().getID());

        if(CommonUtils.AM_DEBUG_debug) {
            String ss = "";
            ss += "AM: Sending AAI Assignment from " + aai.senderID + " is: " + "\n";

            for (EntryPointAgents entryPointAgents : aai.entryPointAgents) {
                ss += "EP: " + entryPointAgents.entrypoint + " Agents: ";
                for (long agentID : entryPointAgents.agents) {
                    ss += agentID + ", ";
                }
                ss += "\n";
            }
            System.out.println(ss);
        }

        this.ae.getCommunicator().sendAllocationAuthority(aai);
    }
}
