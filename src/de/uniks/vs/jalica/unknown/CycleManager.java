package de.uniks.vs.jalica.unknown;

import de.uniks.vs.jalica.teamobserver.PlanRepository;

import java.util.Set;
import java.util.Vector;

/**
 * Created by alex on 27.07.17.
 */
public class CycleManager {

    private AllocationAuthorityInfo fixedAllocation;
    private CycleState state;
    private RunningPlan rp;
    private PlanRepository pr;
    private int myID;
    private boolean enabled;
    private int newestAllocationDifference;
    private Vector<AllocationDifference> allocationHistory;

    public boolean isOverridden() {
        return this.state == CycleState.overridden && this.fixedAllocation != null;
    }

    public boolean setAssignment() {
//#ifdef CM_DEBUG
        System.out.println( "CM: Setting authorative assignment for plan " + rp.getPlan().getName() );
		if (rp.getPlan().getName() == "AuthorityTest") {
            System.out.println( "CM: Changing AuthorityTest ");
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

    public void setNewAllocDiff(Assignment oldAss, Assignment newAss, AllocationDifference.Reason reas) {

        if (!enabled)
        {
            return;
        }
        if (oldAss == null)
        {
            return;
        }
//        lock_guard<mutex> lock(this.allocationHistoryMutex);
        try
        {
            this.newestAllocationDifference = (this.newestAllocationDifference + 1) % this.allocationHistory.size();
            this.allocationHistory.get(this.newestAllocationDifference).reset();

            EntryPoint ep;
            //for (EntryPoint* ep : (*oldAss.getEntryPoints()))
            for (short i = 0; i < oldAss.getEntryPointCount(); i++)
            {
                ep = oldAss.getEpRobotsMapping().getEp(i);

                Vector<Integer> newRobots = newAss.getRobotsWorking(ep);
                Vector<Integer> oldRobots = oldAss.getRobotsWorking(ep);
                for (int oldId : (oldRobots))
                {
                    if (newRobots == null || CommonUtils.find(newRobots,0, newRobots.size()-1, oldId) == newRobots.lastElement())
                    {
                        this.allocationHistory.get(this.newestAllocationDifference).getSubtractions().add(
                            new EntryPointRobotPair(ep, oldId));
                    }
                }
                if (newRobots != null)
                {
                    for (int newId : (newRobots))
                    {
                        if (CommonUtils.find(oldRobots, 0, oldRobots.size()-1, newId) == oldRobots.lastElement())
                        {
                            this.allocationHistory.get(this.newestAllocationDifference).getAdditions().add(
                                new EntryPointRobotPair(ep, newId));
                        }
                    }
                }

            }
            this.allocationHistory.get(this.newestAllocationDifference).setReason(reas);
//#ifdef CM_DEBUG
            System.out.println( "CM: SetNewAllDiff(b): " + this.allocationHistory.get(this.newestAllocationDifference).toString() );
//#endif
        }
        catch (Exception e)
        {
            System.err.println( "Exception in Alloc Difference Calculation:" );
            System.err.println( e.getMessage());

        }
    }
}
