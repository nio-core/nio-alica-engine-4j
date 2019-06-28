package de.uniks.vs.jalica.engine.taskassignment;

import de.uniks.vs.jalica.common.utils.CommonUtils;
import de.uniks.vs.jalica.engine.AlicaEngine;
import de.uniks.vs.jalica.engine.Assignment;
import de.uniks.vs.jalica.engine.ITaskAssignment;
import de.uniks.vs.jalica.engine.SimplePlanTree;
import de.uniks.vs.jalica.engine.collections.SuccessCollection;
import de.uniks.vs.jalica.engine.model.Plan;
import de.uniks.vs.jalica.engine.planselection.PartialAssignment;
import de.uniks.vs.jalica.engine.planselection.PartialAssignmentPool;
import de.uniks.vs.jalica.engine.teammanagement.TeamManager;
import de.uniks.vs.jalica.engine.teammanagement.TeamObserver;

import java.util.*;

/**
 * Created by alex on 21.07.17.
 */
public class TaskAssignment implements ITaskAssignment {

    private TeamManager teamManager;
    private TeamObserver teamObserver;
    private PartialAssignmentPool partialAssignmentPool;
    private ArrayList<Plan> plans;
    private ArrayList<Long> agents;
    private ArrayList<SuccessCollection> successData;
    private ArrayList<PartialAssignment> fringe;

    private int expansionCount;

    public TaskAssignment(AlicaEngine engine, ArrayList<Plan> planList, ArrayList<Long> paraAgents, PartialAssignmentPool partialAssignmentPool) {
        this.agents = paraAgents;
        this.plans = planList;
        this.teamObserver = engine.getTeamObserver();
        this.teamManager = engine.getTeamManager();
        this.partialAssignmentPool = partialAssignmentPool;
        // sort agent ids ascending
        Collections.sort(agents);
        this.successData = new ArrayList<>(this.plans.size());
        this.fringe = new ArrayList<>(this.plans.size());

        for (Plan curPlan : this.plans) {
            // prep successinfo for this plan
            this.successData.add(teamObserver.createSuccessCollection(curPlan));
            // allow caching of eval data
            curPlan.getUtilityFunction().cacheEvalData();
            // seed the fringe with a partial assignment
            PartialAssignment curPa = this.partialAssignmentPool.getNext();
            curPa.prepare(curPlan, this);
            this.fringe.add(curPa);
        }
        this.fringe.sort(PartialAssignment::compareTo);
    }

    public void preassignOtherAgents() {
        // TODO: fix this call
        HashMap<Long, SimplePlanTree> simplePlanTreeMap = this.teamObserver.getTeamPlanTrees();
        // this call should only be made before the search starts
        assert (this.fringe.size() == this.plans.size());
        // ASSIGN PREASSIGNED OTHER ROBOTS
        int i = 0;
        boolean changed = false;
        for (PartialAssignment curPa : this.fringe) {
            if (addAlreadyAssignedRobots(curPa, simplePlanTreeMap)) {
                // revaluate this pa
                curPa.evaluate(null);
                changed = true;
            }
            i++;
        }
        if (changed) {
            this.fringe.sort(PartialAssignment::compareTo);
        }
    }

    public Assignment getNextBestAssignment(Assignment oldAss) {
        CommonUtils.aboutCallNotification("TA: Calculating next best PartialAssignment...");
        PartialAssignment calculatedPa = calcNextBestPartialAssignment(oldAss);

        if (calculatedPa == null) {
            return new Assignment();
        }
        CommonUtils.aboutCallNotification("TA: ... calculated this PartialAssignment:\n" + calculatedPa);
        Assignment newAss = new Assignment(calculatedPa);
        CommonUtils.aboutCallNotification("TA: Return this Assignment to PS:" + newAss);
        return newAss;
    }

    public String toString() {
        String  ss = "";
        ss += "\n";
        ss +=  "--------------------TA:--------------------" +"\n";
        ss +=  "NumRobots: " + this.agents.size() +"\n";
        ss +=  "RobotIDs: ";
        for (long id : this.agents) {
        ss +=  id + " ";
    }
        ss +=  "\n";
        ss +=  "Initial Fringe (Count " + this.fringe.size() + "):" +"\n";
        ss +=  "{";
        for (PartialAssignment pa : this.fringe) // Initial PartialAssignments
        {
            ss +=  pa +"\n";
        }
        ss +=  "}" +"\n";
        ss +=  "-------------------------------------------" +"\n";
        return ss;
    }

    PartialAssignment calcNextBestPartialAssignment(Assignment oldAss) {
           PartialAssignment goal = null;

            while (this.fringe.size() > 0 && goal == null) {
                PartialAssignment curPa = this.fringe.get(0);
                this.fringe.remove(0);

                if (CommonUtils.TA_DEBUG_debug)
                    System.out.println("<---\nTA: NEXT PA from fringe:\n" + curPa.toString() + "--->");

                if (curPa.isGoal()) {
                    goal = curPa;
                } else {
                    if (CommonUtils.TA_DEBUG_debug)
                        System.out.println("<---" + "\nTA: BEFORE fringe exp:" + fringe + "--->");

                    curPa.expand(fringe, partialAssignmentPool, oldAss);

                    if (CommonUtils.TA_DEBUG_debug)
                        System.out.println("<---" + "\nTA: AFTER fringe exp:" + "\nTA: fringe size " + this.fringe.size());
                }
                this.expansionCount++;
            }
            return goal;
    }

    boolean addAlreadyAssignedRobots(PartialAssignment pa, HashMap<Long, SimplePlanTree> simplePlanTreeMap) {
        long ownAgentId = this.teamManager.getLocalAgentID();
        boolean haveToRevalute = false;

        for (int i = 0; i < this.agents.size(); i++) {

            if (ownAgentId == this.agents.get(i)) {
                continue;
            }
            SimplePlanTree iter = simplePlanTreeMap.get(agents.get(i));

            if (iter != null) {

                if (pa.addIfAlreadyAssigned(iter, this.agents.get(i), i)) {
                    haveToRevalute = true;
                }
            }
        }
        return haveToRevalute;
    }

    int getExpansionCount()  { return this.expansionCount; }

    void setExpansionCount(int expansionCount) { this.expansionCount = expansionCount; }

    public SuccessCollection getSuccessData(Plan plan) {
        for (int i = 0; i < (plans.size()); ++i) {
            if (plans.get(i) == plan) {
                return successData.get(i);
            }
        }
        return null;
    }

    public int getAgentCount() {
        return this.agents.size();
    }

    public ArrayList<Long> getAgents() {
        return this.agents;
    }

}
