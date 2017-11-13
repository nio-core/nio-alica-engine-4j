package de.uniks.vs.jalica.unknown;

import de.uniks.vs.jalica.engine.AlicaEngine;
import de.uniks.vs.jalica.supplementary.SystemConfig;
import de.uniks.vs.jalica.teamobserver.PlanRepository;

import java.util.Set;
import java.util.Vector;

import static jdk.nashorn.internal.objects.NativeMath.min;

/**
 * Created by alex on 27.07.17.
 */
public class CycleManager {


    enum CycleState
    {
        observing, overridden, overriding;
    };

    private int historySize;
    private AlicaEngine ae;
    private int maxAllocationCycles;
    private AllocationAuthorityInfo fixedAllocation;
    private CycleState state;
    private RunningPlan rp;
    private PlanRepository pr;
    private int myID;
    private boolean enabled;
    private int newestAllocationDifference;
    private double intervalIncFactor;
    private double intervalDecFactor;
    private AlicaTime minimalOverrideTimeInterval;
    private AlicaTime maximalOverrideTimeInterval;
    private AlicaTime overrideShoutInterval;
    private AlicaTime overrideWaitInterval;
    private AlicaTime overrideShoutTime;
    private AlicaTime overrideTimestamp;

    private Vector<AllocationDifference> allocationHistory = new Vector<>();

    public CycleManager(AlicaEngine ae, RunningPlan p) {
        SystemConfig sc = SystemConfig.getInstance();
        maxAllocationCycles = Integer.valueOf(sc.get("Alica").get("Alica.CycleDetection.CycleCount"));
        enabled = Boolean.valueOf(sc.get("Alica").get("Alica.CycleDetection.Enabled"));
        minimalOverrideTimeInterval = new AlicaTime(Long.valueOf(sc.get("Alica").get("Alica.CycleDetection.MinimalAuthorityTimeInterval")) * 1000000);
        maximalOverrideTimeInterval = new AlicaTime(Long.valueOf(sc.get("Alica").get("Alica.CycleDetection.MaximalAuthorityTimeInterval")) * 1000000);
        overrideShoutInterval = new AlicaTime(Long.valueOf(sc.get("Alica").get("Alica.CycleDetection.MessageTimeInterval")) * 1000000);
        overrideWaitInterval = new AlicaTime(Long.valueOf(sc.get("Alica").get("Alica.CycleDetection.MessageWaitTimeInterval")) * 1000000);
        historySize = Integer.valueOf(sc.get("Alica").get("Alica.CycleDetection.HistorySize"));

        this.ae = ae;
        this.intervalIncFactor = Double.valueOf(sc.get("Alica").get("Alica.CycleDetection.IntervalIncreaseFactor"));
        this.intervalDecFactor = Double.valueOf(sc.get("Alica").get("Alica.CycleDetection.IntervalDecreaseFactor"));

//        this.allocationHistory.setSize(this.historySize);
        for (int i = 0; i < this.historySize; i++)
        {
            this.allocationHistory.add(i, new AllocationDifference());
        }
        this.newestAllocationDifference = 0;
        this.state = CycleState.observing;
        this.rp = p;
        this.myID = ae.getTeamObserver().getOwnId();
        this.pr = ae.getPlanRepository();
        this.overrideTimestamp = new AlicaTime(0);
        this.overrideShoutTime = new AlicaTime(0);
    }

    public boolean isOverridden() {
        return this.state == CycleState.overridden && this.fixedAllocation != null;
    }

    public boolean setAssignment() {
//#ifdef CM_DEBUG
        if (CommonUtils.CM_DEBUG_debug) System.out.println( "CM: Setting authorative assignment for plan " + rp.getPlan().getName() );

        if (rp.getPlan().getName() == "AuthorityTest") {
            if (CommonUtils.CM_DEBUG_debug) System.out.println( "CM: Changing AuthorityTest ");
    }
//#endif
    EntryPoint myEntryPoint = null;
		if (this.fixedAllocation == null)
    {
        return false;
    }
        boolean modifiedSelf = false;
        boolean modified = false;
		if (this.fixedAllocation.planId != rp.getPlan().getId())
    {//Plantype case
        if (rp.getPlanType().getId() != this.fixedAllocation.planType)
        {
            return false;
        }
        Plan newPlan = null;
        for (Plan p : rp.getPlanType().getPlans())
        {
            if (p.getId() == this.fixedAllocation.planId)
            {
                newPlan = p;
                rp.setPlan(p);
                break;
            }
        }
        rp.setAssignment(new Assignment(newPlan, this.fixedAllocation));
        for (EntryPointRobots epr : this.fixedAllocation.entryPointRobots)
        {
            if (CommonUtils.find(epr.robots, 0, epr.robots.size()-1, myID) != epr.robots.lastElement())
            {
                myEntryPoint = pr.getEntryPoints().get(epr.entrypoint);
            }
        }

        modifiedSelf = true;

    }
		else
    {
        for (EntryPointRobots epr : this.fixedAllocation.entryPointRobots)
        {
            for (int robot : epr.robots)
            {
                EntryPoint e = pr.getEntryPoints().get(epr.entrypoint);
                boolean changed = rp.getAssignment().updateRobot(robot, e);
                if (changed)
                {
                    if (robot == myID)
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
        rp.setOwnEntryPoint(myEntryPoint);
        rp.deactivateChildren();
        rp.clearChildren();
        rp.clearFailedChildren();
        rp.setAllocationNeeded(true);

    }
		else
    {
        if (rp.getActiveState() != null)
        {
            Set<Integer> robotsJoined = rp.getAssignment().getRobotStateMapping().getRobotsInState(rp.getActiveState());
            for (RunningPlan c : rp.getChildren())
            {
                c.limitToRobots(robotsJoined);
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
        if (CommonUtils.CM_DEBUG_debug) System.out.println("CM: SetNewAllDiff(a): " + aldif.toString()  + " OWN ROBOT ID " + this.rp.getOwnID());
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
            for (short i = 0; i < oldAss.getEntryPointCount(); i++) {
                ep = oldAss.getEpRobotsMapping().getEp(i);

                Vector<Integer> newRobots = newAss.getRobotsWorking(ep);
                Vector<Integer> oldRobots = oldAss.getRobotsWorking(ep);

                for (int oldId : (oldRobots)) {

                    if (newRobots == null || CommonUtils.find(newRobots,0, newRobots.size()-1, oldId) == newRobots.lastElement()) {
                        this.allocationHistory.get(this.newestAllocationDifference).getSubtractions().add(
                            new EntryPointRobotPair(ep, oldId));
                    }
                }

                if (newRobots != null) {

                    for (int newId : (newRobots)) {

                        if (CommonUtils.find(oldRobots, 0, oldRobots.size()-1, newId) == oldRobots.lastElement()) {
                            this.allocationHistory.get(this.newestAllocationDifference).getAdditions().add(
                                new EntryPointRobotPair(ep, newId));
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
            System.err.println( "Exception in Alloc Difference Calculation:" );
            System.err.println( e.getMessage());

        }
    }

    public boolean haveAuthority() {
        return this.state == CycleState.overriding;
    }

    public boolean needsSending() {
        return this.state == CycleState.overriding
                && (this.overrideShoutTime.time + overrideShoutInterval.time < ae.getIAlicaClock().now().time);
    }

    public void sent() {
        this.overrideShoutTime = ae.getIAlicaClock().now();
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
//#ifdef CM_DEBUG
            if (CommonUtils.CM_DEBUG_debug) System.out.println("CM: Assignment overridden in " + this.rp.getPlan().getName());
//#endif
            this.state = CycleState.overridden;
            this.overrideShoutTime = ae.getIAlicaClock().now();
            this.fixedAllocation = aai;
        }
        else
        {
            System.out.println("CM: Rcv: Rejecting Authority!" );
            if (this.state != CycleState.overriding)
            {
//#ifdef CM_DEBUG
                if (CommonUtils.CM_DEBUG_debug) System.out.println("CM: Overriding assignment of " + this.rp.getPlan().getName() );
//#endif
                this.state = CycleState.overriding;
                this.rp.getPlan().setAuthorityTimeInterval( new AlicaTime(
                    Math.min(maximalOverrideTimeInterval.time, (this.rp.getPlan().getAuthorityTimeInterval().time * intervalIncFactor))));
                this.overrideTimestamp = ae.getIAlicaClock().now();
                this.overrideShoutTime.time = 0;
            }
        }
    }

    public void update() {

        if (!this.enabled) {
            return;
        }

        if (this.rp.isBehaviour()) {
            return;
        }

        AbstractPlan plan = this.rp.getPlan();

        if (this.state == CycleState.observing) {
            if (detectAllocationCycle())
            {
                System.out.println("CM: Cycle Detected!");

                this.state = CycleState.overriding;
                plan.setAuthorityTimeInterval( new AlicaTime(
                        Math.min(maximalOverrideTimeInterval.time, (plan.getAuthorityTimeInterval().time * intervalIncFactor))));
                this.overrideShoutTime.time = 0;
//#ifdef CM_DEBUG
                if (CommonUtils.CM_DEBUG_debug) System.out.println("Assuming Authority for " + plan.getAuthorityTimeInterval().time / 1000000000.0
                        + "sec!" );
//#endif
                this.overrideTimestamp = ae.getIAlicaClock().now();
            }
            else
            {
                plan.setAuthorityTimeInterval( new AlicaTime(
                        Math.max(minimalOverrideTimeInterval.time, (plan.getAuthorityTimeInterval().time * intervalDecFactor))));
            }
        }
		else
        {
            if (this.state == CycleState.overriding
                && this.overrideTimestamp.time + plan.getAuthorityTimeInterval().time
                < ae.getIAlicaClock().now().time)
            {
//#ifdef CM_DEBUG
                if (CommonUtils.CM_DEBUG_debug) System.out.println("Resume Observing!" );
//#endif
                this.state = CycleState.observing;
                this.fixedAllocation = null;
            }
			else if (this.state == CycleState.overridden
                && this.overrideShoutTime.time + plan.getAuthorityTimeInterval().time
                < ae.getIAlicaClock().now().time)
            {
//#ifdef CM_DEBUG
                if (CommonUtils.CM_DEBUG_debug)  System.out.println("Resume Observing!" );
//#endif
                this.state = CycleState.observing;
                this.fixedAllocation = null;

            }
        }
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
            if (CommonUtils.CM_REASON_DEBUG_debug)  System.out.println("CM: REASON " + this.allocationHistory.get(i).getReason().name() + " : " + AllocationDifference.Reason.message );

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
