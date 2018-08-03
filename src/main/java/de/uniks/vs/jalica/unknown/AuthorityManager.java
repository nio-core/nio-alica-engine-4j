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
        this.ownID = 0;
    }

    public void init() {
        this.ownID = ae.getTeamObserver().getOwnID();
    }

    public void tick(RunningPlan rp) {
//        #ifdef AM_DEBUG
        if (CommonUtils.AM_DEBUG_debug) System.out.println("AM: Tick called! <<<<<<" );
//#endif
//        lock_guard<mutex> lock(mu);
        processPlan(rp);
        this.queue.clear();
    }

    private void processPlan(RunningPlan rp)
    {
        if (rp == null || rp.isBehaviour())
        {
            return;
        }
        if (rp.getCycleManagement().needsSending())
        {
            sendAllocation(rp);
            rp.getCycleManagement().sent();
        }
//#ifdef AM_DEBUG
        if (CommonUtils.AM_DEBUG_debug) System.out.println("AM: Queue size of AuthorityInfos is " + this.queue.size() );
//#endif
        for (int i = 0; i < this.queue.size(); i++)
        {
            if (authorityMatchesPlan(this.queue.get(i), rp))
            {
//#ifdef AM_DEBUG
                if (CommonUtils.AM_DEBUG_debug)  System.out.println( "AM: Found AuthorityInfo, which matches the plan " + rp.getPlan().getName() );
//#endif
                rp.getCycleManagement().handleAuthorityInfo(this.queue.get(i));
                this.queue.remove(this.queue.get(i));
                i--;
            }
        }
        for (RunningPlan c : rp.getChildren())
        {
            processPlan(c);
        }

    }

    private boolean authorityMatchesPlan(AllocationAuthorityInfo allocationAuthorityInfo, RunningPlan runningPlan) {
        RunningPlan shared = runningPlan.getParent();
//        auto shared = runningPlan.getParent().lock();
//#ifdef AM_DEBUG
		if (runningPlan.getParent() != null) {
            if (CommonUtils.AM_DEBUG_debug) System.out.println( "AM: Parent-WeakPtr is NOT expired!");
            if (CommonUtils.AM_DEBUG_debug) System.out.println( "AM: Parent-ActiveState is: " + (shared.getActiveState() != null ? shared.getActiveState().getID() : null));
            if (CommonUtils.AM_DEBUG_debug) System.out.println( "AM: AAI-ParentState is: " + allocationAuthorityInfo.parentState);
		}
		else {
            if (CommonUtils.AM_DEBUG_debug) System.out.println(  "AM: Parent-WeakPtr is expired!");
            if (CommonUtils.AM_DEBUG_debug) System.out.println(  "AM: Current-ActiveState is: " + runningPlan.getActiveState().getID());
            if (CommonUtils.AM_DEBUG_debug) System.out.println( "AM: AAI-ParentState is: " + allocationAuthorityInfo.parentState);
		}
//#endif

        if ((runningPlan.getParent() != null && allocationAuthorityInfo.parentState == -1)
                || (runningPlan.getParent() == null && shared.getActiveState() != null && shared.getActiveState().getID() == allocationAuthorityInfo.parentState)) {

            if (runningPlan.getPlan().getID() == allocationAuthorityInfo.planId) {
                return true;
            }
			else if (allocationAuthorityInfo.planType != -1 && runningPlan.getPlanType() != null && runningPlan.getPlanType().getID() == allocationAuthorityInfo.planType) {
                return true;
            }
        }
        return false;
    }

    public void handleIncomingAuthorityMessage(AllocationAuthorityInfo aai) {

        if (ae.getTeamObserver().isRobotIgnored(aai.senderID)) {
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
//#ifdef AM_DEBUG
        if (CommonUtils.AM_DEBUG_debug) {
            String ss = "";
            ss += "AM: Received AAI Assignment from " + aai.senderID + " is: ";
            for (EntryPointAgents epRobots : aai.entryPointAgents) {
                ss += "EP: " + epRobots.entrypoint + " Robots: ";
                for (long robot : epRobots.agents) {
                    ss += robot + ", ";
                }
                ss += "\n";
            }
            System.out.println(ss);
        }
//#endif
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
            EntryPointAgents epRobots = new EntryPointAgents();
            epRobots.entrypoint = ass.getEpAgentsMapping().getEp(i).getID();

            for (long robot : ass.getAgentsWorking(epRobots.entrypoint)) {
                epRobots.agents.add(robot);
            }
            aai.entryPointAgents.add(epRobots);
        }

        RunningPlan shared = p.getParent();
        aai.parentState = ((shared == null || shared.getActiveState() == null) ? -1 : shared.getActiveState().getID());
        aai.planId = p.getPlan().getID();
        aai.authority = this.ownID;
        aai.senderID = this.ownID;
        aai.planType = (p.getPlanType() == null ? -1 : p.getPlanType().getID());

        if(CommonUtils.AM_DEBUG_debug) {
            String ss = "";
            ss += "AM: Sending AAI Assignment from " + aai.senderID + " is: " + "\n";

            for (EntryPointAgents epRobots : aai.entryPointAgents) {
                ss += "EP: " + epRobots.entrypoint + " Robots: ";
                for (long robot : epRobots.agents) {
                    ss += robot + ", ";
                }
                ss += "\n";
            }
            System.out.println(ss);
        }

        this.ae.getCommunicator().sendAllocationAuthority(aai);
    }
}
