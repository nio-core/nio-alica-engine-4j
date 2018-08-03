package de.uniks.vs.jalica.unknown;

import de.uniks.vs.jalica.supplementary.TimerEvent;
import de.uniks.vs.jalica.teamobserver.ITeamObserver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Vector;

import static com.sun.tools.internal.xjc.reader.Ring.begin;

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

    public Assignment getNextBestAssignment(IAssignment oldAss) {
//#ifdef TA_DEBUG
        if (CommonUtils.TA_DEBUG_debug) System.out.println("TA: Calculating next best PartialAssignment...");
//#endif
        PartialAssignment calculatedPa = this.calcNextBestPartialAssignment(oldAss);

        if (calculatedPa == null) {
            return null;
        }

//#ifdef TA_DEBUG
        if (CommonUtils.TA_DEBUG_debug) System.out.println("TA: ... calculated this PartialAssignment:\n" + calculatedPa.toString());
//#endif

        Assignment newAss = new Assignment(calculatedPa);
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
            if (CommonUtils.TA_DEBUG_debug) System.out.println("<---");
            if (CommonUtils.TA_DEBUG_debug) System.out.println("TA: NEXT PA from fringe:" );
            if (CommonUtils.TA_DEBUG_debug) System.out.println(currentPartialAssignment.toString() + "--->" );
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
            ArrayList<PartialAssignment> newPas = currentPartialAssignment.expand();

//#ifdef EXPANSIONEVAL
            expansionCount++;
//#endif
            // Every just expanded partial assignment must get an updated utility

            for (int i = 0; i < newPas.size(); i++)
            {
                // Update the utility values
//                auto iter = newPas->begin();
//                PartialAssignment iter = newPas.get(0);
//                CommonUtils.advance(iter, i);
                PartialAssignment iter = newPas.get(i);
                iter.getUtilFunc().updateAssignment(iter, oldAss);

                if (iter.getMax() != -1) // add this partial assignment only, if all assigned robots does not have a priority of -1 for any task
                {
                    // Add to search fringe
                    this.fringe.add(iter);
                }
            }
            CommonUtils.stable_sort(fringe, 0, fringe.size()-1);

//#ifdef TA_DEBUG
//            cout << "<---" << endl;
//            cout << "TA: AFTER fringe exp:" << endl;
//            cout << "TA: fringe size " << this->fringe.size() << endl;
//            for(int i = 0; i < this->fringe.size(); i++)
//            {
//                cout << this->fringe[i]->toString();
//            }
//            cout << "--->" << endl;
//#endif
        }
        return goal;
    }

}
