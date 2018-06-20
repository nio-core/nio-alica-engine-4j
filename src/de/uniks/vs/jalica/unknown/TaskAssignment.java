package de.uniks.vs.jalica.unknown;

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
    private Vector<Integer> agents;
    private Vector<PartialAssignment> fringe;

    public TaskAssignment(PartialAssignmentPool pap, ITeamObserver to,
                          ArrayList<Plan> planList, Vector<Integer> paraAgents, boolean preassignOtherAgents) {
//        #ifdef EXPANSIONEVAL
        this.expansionCount = 0;
//#endif
        this.planList = planList;
        this.to = to;
        this.agents = new Vector<Integer> (paraAgents.size());
        int k = 0;

        for (int i : paraAgents) {
            this.agents.add(k++,i);
        }
        // sort agent ids ascending
        Collections.sort(agents);
        this.fringe = new Vector<PartialAssignment>();
        LinkedHashMap<Integer, SimplePlanTree> simplePlanTreeMap = to.getTeamPlanTrees();
        PartialAssignment curPa;
        for (Plan curPlan : this.planList)
        {
            // CACHE EVALUATION DATA IN EACH USUMMAND
            curPlan.getUtilityFunction().cacheEvalData();

            // CREATE INITIAL PARTIAL ASSIGNMENTS
            curPa = PartialAssignment.getNew(pap, this.agents, curPlan, to.getSuccessCollection(curPlan));

            // ASSIGN PREASSIGNED OTHER AGENTS
            if (preassignOtherAgents)
            {

                if (this.addAlreadyAssignedAgents(curPa, (simplePlanTreeMap)))
                {
                    // revaluate this pa
                    curPlan.getUtilityFunction().updateAssignment(curPa, null);
                }
            }
            this.fringe.add(curPa);
        }
    }

    private boolean addAlreadyAssignedAgents(PartialAssignment pa, LinkedHashMap<Integer, SimplePlanTree> simplePlanTreeMap) {
        int ownAgentID = to.getOwnID();
        boolean haveToRevalute = false;
        SimplePlanTree spt = null;
        for (int agent : (this.agents))
        {
            if (ownAgentID == agent)
            {
                continue;
            }
            SimplePlanTree iter = simplePlanTreeMap.get(agent);

            // TODO: fix work around
            SimplePlanTree last = null;
            for (Integer key : simplePlanTreeMap.keySet()) {
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
        System.out.println("TA: Calculating next best PartialAssignment...");
//#endif
        PartialAssignment calculatedPa = this.calcNextBestPartialAssignment(oldAss);

        if (calculatedPa == null) {
            return null;
        }
//#ifdef TA_DEBUG
        System.out.println("TA: ... calculated this PartialAssignment:\n" + calculatedPa.toString());
//#endif

        Assignment newAss = new Assignment(calculatedPa);
//#ifdef TA_DEBUG
        System.out.println("TA: Return this Assignment to PS:" + newAss.toString());
//#endif
        return newAss;
    }

    PartialAssignment calcNextBestPartialAssignment(IAssignment oldAss) {
        PartialAssignment curPa = null;
        PartialAssignment goal = null;
        while (this.fringe.size() > 0 && goal == null) {
            curPa = this.fringe.get(0);
//            this->fringe.erase(this->fringe.begin());
            this.fringe.remove(0);
//#ifdef TA_DEBUG
            System.out.println("<---");
            System.out.println("TA: NEXT PA from fringe:" );
            System.out.println(curPa.toString() + "--->" );
//#endif
            // Check if it is a goal
            if (curPa.isGoal()) {
                // Save the goal in result
                goal = curPa;
            }
//#ifdef TA_DEBUG
            System.out.println( "<---" );
            System.out.println( "TA: BEFORE fringe exp:" );
            System.out.println( "TA: robotID " + this.to.getOwnID() );

            for(int i = 0; i < this.fringe.size(); i++) {
                System.out.print( this.fringe.get(i).toString());
            }
            System.out.println( "--->");
//#endif
            // Expand for the next search (maybe necessary later)
            ArrayList<PartialAssignment> newPas = curPa.expand();
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
