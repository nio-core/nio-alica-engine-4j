package de.uniks.vs.jalica.unknown;

import de.uniks.vs.jalica.teamobserver.ITeamObserver;
import de.uniks.vs.jalica.teamobserver.TeamObserver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Vector;

/**
 * Created by alex on 21.07.17.
 */
public class TaskAssignment implements ITaskAssignment {

    private final int expansionCount;
    private  Vector<PartialAssignment> fringe;
    private  Vector<Integer> robots;
    private  ITeamObserver to;
    private ArrayList<Plan> planList;

    public TaskAssignment(PartialAssignmentPool pap, ITeamObserver to,
                          ArrayList<Plan> planList, Vector<Integer> paraRobots, boolean preassignOtherRobots) {
//        #ifdef EXPANSIONEVAL
        this.expansionCount = 0;
//#endif
        this.planList = planList;
        this.to = to;
        this.robots = new Vector<Integer> (paraRobots.size());
        int k = 0;
        for (int i : paraRobots)
        {
            this.robots.set(k++,i);
        }
        // sort robot ids ascending
        Collections.sort(robots);
        this.fringe = new Vector<PartialAssignment>();
        LinkedHashMap<Integer, SimplePlanTree> simplePlanTreeMap = to.getTeamPlanTrees();
        PartialAssignment curPa;
        for (Plan curPlan : this.planList)
        {
            // CACHE EVALUATION DATA IN EACH USUMMAND
            curPlan.getUtilityFunction().cacheEvalData();

            // CREATE INITIAL PARTIAL ASSIGNMENTS
            curPa = PartialAssignment.getNew(pap, this.robots, curPlan, to.getSuccessCollection(curPlan));

            // ASSIGN PREASSIGNED OTHER ROBOTS
            if (preassignOtherRobots)
            {

                if (this.addAlreadyAssignedRobots(curPa, (simplePlanTreeMap)))
                {
                    // revaluate this pa
                    curPlan.getUtilityFunction().updateAssignment(curPa, null);
                }
            }
            this.fringe.add(curPa);
        }
    }

    private boolean addAlreadyAssignedRobots(PartialAssignment pa, LinkedHashMap<Integer, SimplePlanTree> simplePlanTreeMap) {
        int ownRobotId = to.getOwnId();
        boolean haveToRevalute = false;
        SimplePlanTree spt = null;
        for (int robot : (this.robots))
        {
            if (ownRobotId == robot)
            {
                continue;
            }
            SimplePlanTree iter = simplePlanTreeMap.get(robot);

            // TODO: fix work around
            SimplePlanTree last = null;
            for (Integer key : simplePlanTreeMap.keySet()) {
                last = simplePlanTreeMap.get(key);
            }

            if (iter != last)
            {
                spt = last;
                if (pa.addIfAlreadyAssigned(spt, robot))
                {
                    haveToRevalute = true;
                }
            }
        }
        return haveToRevalute;
    }

    public Assignment getNextBestAssignment(Assignment ass) {
        CommonUtils.aboutNoImpl();
        return null;
    }
}
