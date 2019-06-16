package de.uniks.vs.jalica.engine.taskassignment;

import de.uniks.vs.jalica.common.utils.CommonUtils;
import de.uniks.vs.jalica.engine.Assignment;
import de.uniks.vs.jalica.engine.ITaskAssignment;
import de.uniks.vs.jalica.engine.ITeamObserver;
import de.uniks.vs.jalica.engine.SimplePlanTree;
import de.uniks.vs.jalica.engine.model.Plan;
import de.uniks.vs.jalica.engine.planselection.IAssignment;
import de.uniks.vs.jalica.engine.planselection.PartialAssignment;
import de.uniks.vs.jalica.engine.planselection.PartialAssignmentPool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Vector;

/**
 * Created by alex on 21.07.17.
 */
public class TaskAssignment implements ITaskAssignment {

    private int expansionCount;
    private ITeamObserver teamObserver;
    private ArrayList<Plan> planList;
    private ArrayList<Long> agents;
    private ArrayList<PartialAssignment> fringe;
    private PartialAssignmentPool partialAssignmentPool;

    public TaskAssignment(PartialAssignmentPool partialAssignmentPool, ITeamObserver teamObserver,
                          ArrayList<Plan> planList, Vector<Long> paraAgents, boolean preassignOtherAgents) {
        this.partialAssignmentPool = partialAssignmentPool;
        this.planList = planList;
        this.teamObserver = teamObserver;

        this.expansionCount = 0;
        this.agents = new ArrayList<> (/*paraAgents.size()*/);
        int k = 0;

        for (long i : paraAgents) {
            this.agents.add(k++, i);
        }
        // sort agent ids ascending
        Collections.sort(agents);
        this.fringe = new ArrayList<PartialAssignment>();
        LinkedHashMap<Long, SimplePlanTree> simplePlanTreeMap = teamObserver.getTeamPlanTrees();
        PartialAssignment currentPartialAssignment;
        for (Plan curPlan : this.planList)
        {
            // CACHE EVALUATION DATA IN EACH USUMMAND
            curPlan.getUtilityFunction().cacheEvalData();

            // CREATE INITIAL PARTIAL ASSIGNMENTS
            currentPartialAssignment = PartialAssignment.getNew(partialAssignmentPool, this.agents, curPlan, teamObserver.getSuccessCollection(curPlan));

            // ASSIGN PREASSIGNED OTHER AGENTS
            if (preassignOtherAgents)
            {

                if (this.addAlreadyAssignedAgents(currentPartialAssignment, simplePlanTreeMap))
                {
                    // revaluate this pa
                    curPlan.getUtilityFunction().updateAssignment(currentPartialAssignment, null);
                }
            }
            this.fringe.add(currentPartialAssignment);
        }
    }

    private boolean addAlreadyAssignedAgents(PartialAssignment pa, LinkedHashMap<Long, SimplePlanTree> simplePlanTreeMap) {
        long ownAgentID = teamObserver.getOwnID();
        boolean haveToRevalute = false;
        SimplePlanTree spt = null;
        for (long agent : (this.agents))
        {
            if (ownAgentID == agent)
            {
                continue;
            }
            SimplePlanTree iter = simplePlanTreeMap.get(agent);

            // TODO: fix work around
            SimplePlanTree last = null;
            for (long key : simplePlanTreeMap.keySet()) {
                last = simplePlanTreeMap.get(key);
            }

            if (iter != last)
            {
                spt = last;
                if (pa.addIfAlreadyAssigned(spt, agent))
                {
                    haveToRevalute = true;
                }
            }
        }
        return haveToRevalute;
    }

    public Assignment getNextBestAssignment(IAssignment oldPartialAssignment) {
        if (CommonUtils.TA_DEBUG_debug) System.out.println("TA: Calculating next best PartialAssignment...");

        PartialAssignment calculatedPartialAssignment = this.calcNextBestPartialAssignment(oldPartialAssignment);

        if (calculatedPartialAssignment == null)
            return null;

        if (CommonUtils.TA_DEBUG_debug) System.out.println("TA: ... calculated this PartialAssignment:\n" + calculatedPartialAssignment.toString());

        Assignment newAss = new Assignment(calculatedPartialAssignment);

        if (CommonUtils.TA_DEBUG_debug) System.out.println("TA: Return this Assignment teamObserver PS:" + newAss.toString());

        return newAss;
    }

    PartialAssignment calcNextBestPartialAssignment(IAssignment oldAssignment) {
        PartialAssignment goal = null;
//        PartialAssignment currentPartialAssignment = null;

        while (this.fringe.size() > 0 && goal == null) {
            PartialAssignment currentPartialAssignment = this.fringe.get(0);
            this.fringe.remove(0);

            if (CommonUtils.TA_DEBUG_debug)  System.out.println("<---\nTA: NEXT PA from fringe:\n" + currentPartialAssignment.toString() + "--->");

            if (currentPartialAssignment.isGoal()) {
                goal = currentPartialAssignment;
            } else {
                if (CommonUtils.TA_DEBUG_debug) System.out.println("<---" + "\nTA: BEFORE fringe exp:" + fringe + "--->");

//                currentPartialAssignment.expand(fringe, partialAssignmentPool, oldAssignment);

                if (CommonUtils.TA_DEBUG_debug)  System.out.println("<---" + "\nTA: AFTER fringe exp:" + "\nTA: fringe size " + this.fringe.size());
            }

//            expansionCount++;
//        }
//        return goal;



            for( int i = 0; i < this.fringe.size(); i++) {
                if (CommonUtils.TA_DEBUG_debug) System.out.print( this.fringe.get(i).toString());
            }

            // Expand for the next search (maybe necessary later)
            ArrayList<PartialAssignment> newPartialAssignment = currentPartialAssignment.expand();

            expansionCount++;
            // Every just expanded partial assignment must get an updated utility

            for (int i = 0; i < newPartialAssignment.size(); i++)
            {
                // Update the utility values
//                auto partialAssignment = newPartialAssignment->begin();
//                PartialAssignment partialAssignment = newPartialAssignment.get(0);
//                CommonUtils.advance(partialAssignment, i);
                PartialAssignment partialAssignment = newPartialAssignment.get(i);
                partialAssignment.getUtilFunc().updateAssignment(partialAssignment, oldAssignment);

                if (partialAssignment.getMax() != -1) // add this partial assignment only, if all assigned agents does not have a priority of -1 for any task
                {
                    // Add teamObserver search fringe
                    this.fringe.add(partialAssignment);
                }
            }
            CommonUtils.stable_sort(fringe, 0, fringe.size()-1);

            if (CommonUtils.TA_DEBUG_debug) {
                System.out.println("<---"+"\nTA: AFTER fringe exp:"+"\nTA: fringe size " + this.fringe.size());

                for(int i = 0; i < this.fringe.size(); i++) {
                    System.out.print("TA:      " + this.fringe.get(i).toString());
                }
                System.out.println("--->");
            }
        }
        return goal;
    }

}
