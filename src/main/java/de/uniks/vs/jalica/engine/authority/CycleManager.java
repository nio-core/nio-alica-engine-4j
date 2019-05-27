package de.uniks.vs.jalica.engine.authority;

import de.uniks.vs.jalica.engine.AlicaEngine;
import de.uniks.vs.jalica.engine.Assignment;
import de.uniks.vs.jalica.engine.RunningPlan;
import de.uniks.vs.jalica.supplementary.SystemConfig;
import de.uniks.vs.jalica.engine.PlanRepository;
import de.uniks.vs.jalica.engine.containers.messages.AllocationAuthorityInfo;
import de.uniks.vs.jalica.engine.model.AbstractPlan;
import de.uniks.vs.jalica.engine.model.EntryPoint;
import de.uniks.vs.jalica.engine.model.Plan;
import de.uniks.vs.jalica.engine.AlicaTime;
import de.uniks.vs.jalica.engine.common.CommonUtils;
import de.uniks.vs.jalica.engine.containers.EntryPointAgents;

import java.util.Set;
import java.util.Vector;

import static jdk.nashorn.internal.objects.NativeMath.min;

/**
 * Created by alex on 27.07.17.
 */
public class CycleManager {


    enum CycleState {
        observing, overridden, overriding;
    };

    private int historySize;
    private AlicaEngine alicaEngine;
    private int maxAllocationCycles;
    private AllocationAuthorityInfo fixedAllocation;
    private CycleState state;
    private RunningPlan runningPlan;
    private PlanRepository planRepository;
    private long myID;
    private boolean enabled;
    private int newestAllocationDifference;
    private double intervalIncFactor;
    private double intervalDecFactor;
    private AlicaTime minimalOverrideTimeInterval;
    private AlicaTime maximalOverrideTimeInterval;
    private AlicaTime overrideShoutInterval;
//    private AlicaTime overrideWaitInterval;
    private AlicaTime overrideShoutTime;
    private AlicaTime overrideTimestamp;

    private Vector<AllocationDifference> allocationHistory = new Vector<>();

    public CycleManager(AlicaEngine ae, RunningPlan runningPlan) {
        this.alicaEngine = ae;
        SystemConfig sc = ae.getSystemConfig();
        this.maxAllocationCycles = Integer.valueOf((String) sc.get("Alica").get("Alica.CycleDetection.CycleCount"));
        this.enabled = Boolean.valueOf((String) sc.get("Alica").get("Alica.CycleDetection.Enabled"));

        long minimalOverrideInterval = Long.valueOf((String) sc.get("Alica").get("Alica.CycleDetection.MinimalAuthorityTimeInterval"));
        long maximalAuthorityInterval = Long.valueOf((String) sc.get("Alica").get("Alica.CycleDetection.MaximalAuthorityTimeInterval"));
        long messageInterval = Long.valueOf((String) sc.get("Alica").get("Alica.CycleDetection.MessageTimeInterval"));
        this.minimalOverrideTimeInterval = new AlicaTime().inMilliseconds(minimalOverrideInterval);
        this.maximalOverrideTimeInterval = new AlicaTime().inMilliseconds(maximalAuthorityInterval);
        this.overrideShoutInterval = new AlicaTime().inMilliseconds(messageInterval);
//        overrideWaitInterval = new AlicaTime(Long.valueOf((String) sc.get("Alica").get("Alica.CycleDetection.MessageWaitTimeInterval")) * 1000000);

        this.historySize = Integer.valueOf((String) sc.get("Alica").get("Alica.CycleDetection.HistorySize"));

        this.intervalIncFactor = Double.valueOf((String) sc.get("Alica").get("Alica.CycleDetection.IntervalIncreaseFactor"));
        this.intervalDecFactor = Double.valueOf((String) sc.get("Alica").get("Alica.CycleDetection.IntervalDecreaseFactor"));

//        this.allocationHistory.setSize(this.historySize);
        for (int i = 0; i < this.historySize; i++)
        {
            this.allocationHistory.add(i, new AllocationDifference());
        }
        this.newestAllocationDifference = 0;
        this.setState(CycleState.observing);
        this.runningPlan = runningPlan;
        this.myID = ae.getTeamObserver().getOwnID();
        this.planRepository = ae.getPlanRepository();
        this.overrideTimestamp = new AlicaTime();
        this.overrideShoutTime = new AlicaTime();
    }

    public boolean isOverridden() {
        return this.state == CycleState.overridden && this.fixedAllocation != null;
    }

    public boolean setAssignment() {
//#ifdef CM_DEBUG
        if (CommonUtils.CM_DEBUG_debug) System.out.println( "CM("+this.myID+"): Setting authorative assignment for plan " + runningPlan.getPlan().getName() );

        if (runningPlan.getPlan().getName() == "AuthorityTest") {
            if (CommonUtils.CM_DEBUG_debug) System.out.println( "CM("+this.myID+"): Changing AuthorityTest ");
        }
    //#endif
        EntryPoint myEntryPoint = null;
        if (this.fixedAllocation == null)
        {
            return false;
        }
        boolean modifiedSelf = false;
        boolean modified = false;
        if (this.fixedAllocation.planID != runningPlan.getPlan().getID())
        {//Plantype case
            if (runningPlan.getPlanType().getID() != this.fixedAllocation.planType)
            {
                return false;
            }
            Plan newPlan = null;
            for (Plan p : runningPlan.getPlanType().getPlans())
            {
                if (p.getID() == this.fixedAllocation.planID)
                {
                    newPlan = p;
                    runningPlan.setPlan(p);
                    break;
                }
            }
            runningPlan.setAssignment(new Assignment(newPlan, this.fixedAllocation));
            for (EntryPointAgents epr : this.fixedAllocation.entryPointAgents)
            {
                if (CommonUtils.find(epr.agents, 0, epr.agents.size()-1, myID) != epr.agents.lastElement())
                {
                    myEntryPoint = planRepository.getEntryPoints().get(epr.entrypoint);
                }
            }
            modifiedSelf = true;
        }
        else
        {
            for (EntryPointAgents epr : this.fixedAllocation.entryPointAgents) {

                for (long agent : epr.agents) {
                    EntryPoint e = planRepository.getEntryPoints().get(epr.entrypoint);
                    boolean changed = runningPlan.getAssignment().updateAgent(agent, e);
                    if (changed)
                    {
                        if (agent == myID)
                        {
                            modifiedSelf = true;
                            myEntryPoint = e;
                        }
                        else
                        {
                            modified = true;
                        }
                    }
                }
            }
        }
        if (modifiedSelf)
        {
            runningPlan.setOwnEntryPoint(myEntryPoint);
            runningPlan.deactivateChildren();
            runningPlan.clearChildren();
            runningPlan.clearFailedChildren();
            runningPlan.setAllocationNeeded(true);
        }
        else
        {
            if (runningPlan.getActiveState() != null)
            {
                Set<Long> agentsJoined = runningPlan.getAssignment().getAgentStateMapping().getAgentsInState(runningPlan.getActiveState());
                for (RunningPlan runningPlan : runningPlan.getChildren())
                {
                    runningPlan.limitToAgents(agentsJoined);
                }
            }
        }
		return modifiedSelf || modified;
    }

    public boolean mayDoUtilityCheck() {
        return this.state != CycleState.overridden;
    }

    public void setNewAllocDiff(AllocationDifference aldif) {

        if (!enabled) {
            return;
        }
//        lock_guard<mutex> lock(this.allocationHistoryMutex);

        this.newestAllocationDifference = (this.newestAllocationDifference + 1) % this.allocationHistory.size();
//        AllocationDifference old = this.allocationHistory.get(this.newestAllocationDifference);
        this.allocationHistory.set(this.newestAllocationDifference, aldif);

//#ifdef CM_DEBUG
        if (CommonUtils.CM_DEBUG_debug) System.out.println("CM("+this.myID+"): SetNewAllDiff(a): \n   " + aldif.toString()  + "     OWN AGENT ID " + this.runningPlan.getOwnID());
//#endif

    }

    public void setNewAllocDiff(Assignment oldAss, Assignment newAss, AllocationDifference.Reason reas) {

        if (!enabled) {
            return;
        }
        if (oldAss == null) {
            return;
        }
//        lock_guard<mutex> lock(this.allocationHistoryMutex);
        try
        {
            this.newestAllocationDifference = (this.newestAllocationDifference + 1) % this.allocationHistory.size();
            this.allocationHistory.get(this.newestAllocationDifference).reset();

            EntryPoint ep;
            //for (EntryPoint* ep : (*oldAss.getEntryPoints()))
            for (int i = 0; i < oldAss.getEntryPointCount(); i++) {
                ep = oldAss.getEpAgentsMapping().getEntryPoint(i);

                Vector<Long> newAgents = newAss.getAgentsWorking(ep);
                Vector<Long> oldAgents = oldAss.getAgentsWorking(ep);

                for (long oldId : (oldAgents)) {

                    if (newAgents == null || CommonUtils.find(newAgents,0, newAgents.size()-1, oldId) == newAgents.lastElement()) {
                        this.allocationHistory.get(this.newestAllocationDifference).getSubtractions().add(new EntryPointAgentPair(ep, oldId));
                    }
                }

                if (newAgents != null) {

                    for (long newId : (newAgents)) {
                        Long id = CommonUtils.find(oldAgents, 0, oldAgents.size() - 1, newId);

                        if (id != null) {
                            this.allocationHistory.get(this.newestAllocationDifference).getAdditions().add(new EntryPointAgentPair(ep, newId));
                        }
                    }
                }

            }
            this.allocationHistory.get(this.newestAllocationDifference).setReason(reas);
//#ifdef CM_DEBUG
            if (CommonUtils.CM_DEBUG_debug) System.out.println( "CM: SetNewAllDiff(b): " + this.allocationHistory.get(this.newestAllocationDifference).toString() );
//#endif
        }
        catch (Exception e) {
            System.err.print( "CM("+this.myID+"): Exception in Alloc Difference Calculation: " );
            System.err.println( e.getMessage());

        }
    }

    public boolean haveAuthority() {
        return this.state == CycleState.overriding;
    }

    public boolean needsSending() {
        if (CommonUtils.CM_DEBUG_debug) System.out.println("CM: " + this.overrideShoutTime.time + overrideShoutInterval.time + "<" +alicaEngine.getAlicaClock().now().time
                + " = "+ (this.overrideShoutTime.time + overrideShoutInterval.time < alicaEngine.getAlicaClock().now().time) +"  " +
                " " + ((this.state == CycleState.overriding) ));

        return (this.state == CycleState.overriding )
                && ((this.overrideShoutTime.time + overrideShoutInterval.time) < alicaEngine.getAlicaClock().now().time);
    }

    public void sent() {
        this.overrideShoutTime = alicaEngine.getAlicaClock().now();
    }

    public void handleAuthorityInfo(AllocationAuthorityInfo aai) {
        
        if (!enabled) {
            return;
        }
        
        long rid = aai.authority;
        if (rid == myID)
        {
            return;
        }
        if (rid > myID)
        {
            if (CommonUtils.CM_DEBUG_debug) System.out.println("CM: Assignment overridden in " + this.runningPlan.getPlan().getName());
            this.setState(CycleState.overridden);
            this.overrideShoutTime = alicaEngine.getAlicaClock().now();
            this.fixedAllocation = aai;
        }
        else
        {
            if (CommonUtils.CM_DEBUG_debug) System.out.println("CM: Rcv: Rejecting Authority!" );
            if (this.state != CycleState.overriding)
            {
                if (CommonUtils.CM_DEBUG_debug) System.out.println("CM: Overriding assignment of " + this.runningPlan.getPlan().getName() );
                this.setState(CycleState.overriding);
                this.runningPlan.getPlan().setAuthorityTimeInterval( new AlicaTime().inNanoseconds(
                    Math.min(maximalOverrideTimeInterval.time, (long)(this.runningPlan.getPlan().getAuthorityTimeInterval().time * intervalIncFactor))));
                this.overrideTimestamp = alicaEngine.getAlicaClock().now();
                this.overrideShoutTime.time = 0;
            }
        }
    }

    public void update() {

        if (!this.enabled) {
            return;
        }

        if (this.runningPlan.isBehaviour()) {
            return;
        }

        AbstractPlan plan = this.runningPlan.getPlan();

        if (this.state == CycleState.observing) {
            if (detectAllocationCycle())
            {
                System.err.println("CM("+this.myID+"): Cycle Detected!");

                this.setState(CycleState.overriding);
                plan.setAuthorityTimeInterval( new AlicaTime().inNanoseconds(
                        Math.min(maximalOverrideTimeInterval.time, (long) (plan.getAuthorityTimeInterval().time * intervalIncFactor))));
                this.overrideShoutTime.time = 0;
                if (CommonUtils.CM_DEBUG_debug) System.out.println("CM("+this.myID+"): Assuming Authority for " + plan.getAuthorityTimeInterval().inSeconds()
                        + "sec!" );
                this.overrideTimestamp = alicaEngine.getAlicaClock().now();
            }
            else
            {
                plan.setAuthorityTimeInterval( new AlicaTime().inNanoseconds(
                        Math.max(minimalOverrideTimeInterval.time, (long)(plan.getAuthorityTimeInterval().time * intervalDecFactor))));
            }
        }
		else
        {
            if (this.state == CycleState.overriding
                && this.overrideTimestamp.time + plan.getAuthorityTimeInterval().time
                < alicaEngine.getAlicaClock().now().time)
            {
                if (CommonUtils.CM_DEBUG_debug) System.out.println("CM("+this.myID+"): Resume Observing!" );
                this.setState(CycleState.observing);
                this.fixedAllocation = null;
            }
			else if (this.state == CycleState.overridden
                && this.overrideShoutTime.time + plan.getAuthorityTimeInterval().time
                < alicaEngine.getAlicaClock().now().time)
            {
                if (CommonUtils.CM_DEBUG_debug)  System.out.println("CM("+this.myID+"): Resume Observing!" );
                this.setState(CycleState.observing);
                this.fixedAllocation = null;

            }
        }
    }

    private void setState(CycleState state) {
        if (CommonUtils.CM_DEBUG_debug) System.out.println("CM("+this.myID+"): set state -> " + state.name());

//        if(state != CycleState.observing)
//            System.err.println("CM:  !!!!!!!!!!!!!!!!!!!!!!");

        this.state = state;
    }

    private boolean detectAllocationCycle() {
        //Consists of 1 UtilityChange, m message update
        //after uc, allocation is same again (delta = 0)
        int cyclesFound = 0;
        int count = 0;
        AllocationDifference utChange = null;
        AllocationDifference temp = new AllocationDifference();
//        lock_guard<mutex> lock(this.allocationHistoryMutex);

        for (int i = this.newestAllocationDifference; count < this.allocationHistory.size(); i--) {
            count++;

            if (i < 0) {
                i = this.allocationHistory.size() - 1;
            }
            if (CommonUtils.CM_REASON_DEBUG_debug)  System.out.println("CM("+this.myID+"): REASON " + this.allocationHistory.get(i).getReason().name() + " : " + AllocationDifference.Reason.message );

            if (this.allocationHistory.get(i).getReason() == AllocationDifference.Reason.utility) {

                if (utChange != null) {
                    return false;
                }
                utChange = this.allocationHistory.get(i);
                temp.reset();
                temp.applyDifference(utChange);
            }
            else {

                if (this.allocationHistory.get(i).getReason() == AllocationDifference.Reason.empty) {
                    return false;
                }

                if (utChange == null) {
                    continue;
                }
                temp.applyDifference(this.allocationHistory.get(i));

                if (temp.isEmpty()) {
                    cyclesFound++;

                    if (cyclesFound > maxAllocationCycles) {

                        for (int k = 0; k < this.allocationHistory.size(); k++) {
                            this.allocationHistory.get(k).reset();
                        }
                        return true;
                    }
                    utChange = null;
                }
            }
        }
        return false;
    }

}
