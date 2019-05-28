package de.uniks.vs.jalica.engine.planselection;

import de.uniks.vs.jalica.engine.Assignment;
import de.uniks.vs.jalica.engine.SimplePlanTree;
import de.uniks.vs.jalica.engine.ITeamObserver;
import de.uniks.vs.jalica.engine.model.Plan;
import de.uniks.vs.jalica.common.utils.CommonUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Vector;

/**
 * Created by alex on 21.07.17.
 */
public class TaskAssignment implements ITaskAssignment {

    private int expansionCount;
    private ITeamObserver to;
    private ArrayList<Plan> planList;
    private Vector<Long> agents;
    private Vector<PartialAssignment> fringe;

    public TaskAssignment(PartialAssignmentPool pap, ITeamObserver to,
                          ArrayList<Plan> planList, Vector<Long> paraAgents, boolean preassignOtherAgents) {
//        #ifdef EXPANSIONEVAL
        this.expansionCount = 0;
//#endif
        this.planList = planList;
        this.to = to;
        this.agents = new Vector<Long> (/*paraAgents.size()*/);
        int k = 0;

        for (long i : paraAgents) {
            this.agents.add(k++, i);
        }
        // sort agent ids ascending
        Collections.sort(agents);
        this.fringe = new Vector<PartialAssignment>();
        LinkedHashMap<Long, SimplePlanTree> simplePlanTreeMap = to.getTeamPlanTrees();
        PartialAssignment currentPartialAssignment;
        for (Plan curPlan : this.planList)
        {
            // CACHE EVALUATION DATA IN EACH USUMMAND
            curPlan.getUtilityFunction().cacheEvalData();

            // CREATE INITIAL PARTIAL ASSIGNMENTS
            currentPartialAssignment = PartialAssignment.getNew(pap, this.agents, curPlan, to.getSuccessCollection(curPlan));

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
        long ownAgentID = to.getOwnID();
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
//#ifdef TA_DEBUG
        if (CommonUtils.TA_DEBUG_debug) System.out.println("TA: Calculating next best PartialAssignment...");
//#endif
        PartialAssignment calculatedPartialAssignment = this.calcNextBestPartialAssignment(oldPartialAssignment);

        if (calculatedPartialAssignment == null) {
            return null;
        }

//#ifdef TA_DEBUG
        if (CommonUtils.TA_DEBUG_debug) System.out.println("TA: ... calculated this PartialAssignment:\n" + calculatedPartialAssignment.toString());
//#endif

        Assignment newAss = new Assignment(calculatedPartialAssignment);
//#ifdef TA_DEBUG
        if (CommonUtils.TA_DEBUG_debug) System.out.println("TA: Return this Assignment to PS:" + newAss.toString());
//#endif

        return newAss;
    }

    PartialAssignment calcNextBestPartialAssignment(IAssignment oldAss) {
        PartialAssignment currentPartialAssignment = null;
        PartialAssignment goal = null;

        while (this.fringe.size() > 0 && goal == null) {
            currentPartialAssignment = this.fringe.get(0);
//            this->fringe.erase(this->fringe.begin());
            this.fringe.remove(0);
//#ifdef TA_DEBUG
            if (CommonUtils.TA_DEBUG_debug) {
                System.out.println("<---");
                System.out.println("TA: NEXT PA from fringe:");
                System.out.println(currentPartialAssignment.toString() + "--->");
            }
//#endif
            // Check if it is a goal
            if (currentPartialAssignment.isGoal()) {
                // Save the goal in result
                goal = currentPartialAssignment;
            }

//#ifdef TA_DEBUG
            if (CommonUtils.TA_DEBUG_debug) System.out.println( "<---" );
            if (CommonUtils.TA_DEBUG_debug) System.out.println( "TA: BEFORE fringe exp:" );
            if (CommonUtils.TA_DEBUG_debug) System.out.println( "TA: agentID " + this.to.getOwnID() );

            for(int i = 0; i < this.fringe.size(); i++) {
                if (CommonUtils.TA_DEBUG_debug) System.out.print( this.fringe.get(i).toString());
            }
            if (CommonUtils.TA_DEBUG_debug) System.out.println( "--->");
//#endif
            // Expand for the next search (maybe necessary later)
            ArrayList<PartialAssignment> newPartialAssignment = currentPartialAssignment.expand();

//#ifdef EXPANSIONEVAL
            expansionCount++;
//#endif
            // Every just expanded partial assignment must get an updated utility

            for (int i = 0; i < newPartialAssignment.size(); i++)
            {
                // Update the utility values
//                auto partialAssignment = newPartialAssignment->begin();
//                PartialAssignment partialAssignment = newPartialAssignment.get(0);
//                CommonUtils.advance(partialAssignment, i);
                PartialAssignment partialAssignment = newPartialAssignment.get(i);
                partialAssignment.getUtilFunc().updateAssignment(partialAssignment, oldAss);

                if (partialAssignment.getMax() != -1) // add this partial assignment only, if all assigned robots does not have a priority of -1 for any task
                {
                    // Add to search fringe
                    this.fringe.add(partialAssignment);
                }
            }
            CommonUtils.stable_sort(fringe, 0, fringe.size()-1);

//#ifdef TA_DEBUG
            if (CommonUtils.TA_DEBUG_debug) {
                System.out.println("<---");
                System.out.println("TA: AFTER fringe exp:");
                System.out.println("TA: fringe size " + this.fringe.size());

                for(int i = 0; i < this.fringe.size(); i++) {
                    System.out.print("TA:      " + this.fringe.get(i).toString());
                }
                System.out.println("--->");
            }
//#endif
        }
        return goal;
    }

}
