package de.uniks.vs.jalica.engine.planselection;

import de.uniks.vs.jalica.common.Comparable;
import de.uniks.vs.jalica.common.ExtArrayList;
import de.uniks.vs.jalica.engine.Assignment;
import de.uniks.vs.jalica.engine.UtilityInterval;
import de.uniks.vs.jalica.engine.SimplePlanTree;
import de.uniks.vs.jalica.engine.idmanagement.ID;
import de.uniks.vs.jalica.engine.model.EntryPoint;
import de.uniks.vs.jalica.engine.model.Plan;
import de.uniks.vs.jalica.engine.collections.SuccessCollection;
import de.uniks.vs.jalica.common.utils.CommonUtils;
import de.uniks.vs.jalica.engine.common.DynCardinality;
import de.uniks.vs.jalica.engine.taskassignment.TaskAssignment;

import java.util.ArrayList;

/**
 * Created by alex on 21.07.17.
 * Updated 25.6.19
 */
public class PartialAssignment implements Comparable<PartialAssignment> {

    private static final long PRECISION = 1073741824;
    private static final int INFINIT = Integer.MAX_VALUE;

    static boolean allowIdling = true;

    Plan plan;
    TaskAssignment problem;
    ArrayList<DynCardinality> cardinalities;
    ExtArrayList<Integer> assignment;
    UtilityInterval utility;
    int numAssignedAgents;
    int nextAgentIdx;


    public PartialAssignment() {
        this.plan = null;
        this.problem = null;
        this.numAssignedAgents = 0;
        this.nextAgentIdx = 0;
        this.utility = new UtilityInterval(Double.MIN_VALUE, Double.MAX_VALUE);
        this.cardinalities = new ArrayList<>();
        this.assignment = new ExtArrayList<Integer>(() -> new Integer(-1));
    }


    public void copy(PartialAssignment partialAssignment) {
        this.plan = partialAssignment.plan;
        this.problem = partialAssignment.problem;
        this.cardinalities = new ArrayList<>(partialAssignment.cardinalities);
        this.assignment = new ExtArrayList<Integer>(partialAssignment.assignment, () -> new Integer(-1));
        this.utility = partialAssignment.utility;
        this.numAssignedAgents = partialAssignment.numAssignedAgents;
       this.nextAgentIdx = partialAssignment.nextAgentIdx;

    }

    public boolean isValid() {
        int min = 0;
        for (DynCardinality dc : this.cardinalities) {
            min += dc.getMin();
            if (dc.getMax() < 0) {
                return false;
            }
        }
        return min <= this.problem.getAgentCount() - this.numAssignedAgents;
    }

    public boolean isGoal() {
        // There should be no unassigned agents anymore
        if (this.problem.getAgentCount() != this.numAssignedAgents) {
            return false;
        }
        // Every EntryPoint should be satisfied according to his minCar
        for (DynCardinality dc : this.cardinalities) {
            if (dc.getMin() > 0) {
                return false;
            }
        }
        return true;
    }

    public SuccessCollection getSuccessData() {
        return this.problem.getSuccessData(this.plan);
    }

    public int getAssignedAgentCount(int idx) {
        return this.plan.getEntryPoints().get(idx).getCardinality().getMax() - this.cardinalities.get(idx).getMax();
    }

    void clear() {
        this.plan = null;
        this.problem = null;
        this.numAssignedAgents = 0;
        this.nextAgentIdx = 0;
        this.cardinalities.clear();
        this.assignment.clear();
        this.utility = new UtilityInterval(Double.MIN_VALUE, Double.MAX_VALUE);
    }

    public void prepare(Plan p, TaskAssignment problem) {
        this.plan = p;
        this.problem = problem;
        this.numAssignedAgents = 0;
        this.nextAgentIdx = 0;
        this.assignment.clear();
        this.assignment.resize( problem.getAgentCount());
        this.cardinalities.clear();
        this.cardinalities.ensureCapacity(p.getEntryPoints().size() + (this.allowIdling ? 1 : 0));

        for (EntryPoint ep : p.getEntryPoints()) {
//        this.cardinalities.add(ep.getCardinality() - problem.getSuccessData(p).getAgents(ep).size());
            this.cardinalities.add(ep.getCardinality().decrease(problem.getSuccessData(p).getAgents(ep).size()));
        }

        if (this.allowIdling) {
            this.cardinalities.add(new DynCardinality(0, Integer.MAX_VALUE));
        }
        this.utility = new UtilityInterval(Double.MIN_VALUE, Double.MAX_VALUE);
    }

    public boolean assignUnassignedAgent(int agentIdx, int epIdx) {

        if (this.cardinalities.get(epIdx).getMax() > 0) {
            cardinalities.get(epIdx).decrease();
//            --_cardinalities[epIdx];
            assert (this.assignment.get(agentIdx) < 0); // we assume the agent was unassigned
            this.assignment.add(agentIdx, epIdx);
            if (this.nextAgentIdx == agentIdx) {
                this.nextAgentIdx++;
            }
            this.numAssignedAgents++;
            return true;
        }
        return false;
    }

    public boolean addIfAlreadyAssigned(SimplePlanTree spt, ID agent, int idx) {
        if (spt.getEntryPoint().getPlan() == this.plan) {
            int numEps = this.plan.getEntryPoints().size();

            for (int i = 0; i < numEps; ++i) {
                EntryPoint curEp = this.plan.getEntryPoints().get(i);
                if (spt.getEntryPoint().getID() == curEp.getID()) {
                    return assignUnassignedAgent(idx, i);
                }
            }
            return false;
        }
        // If there are children and we didnt find the robot until now, then go on recursive
        else {
            for (SimplePlanTree sptChild : spt.getChildren()) {
                if (addIfAlreadyAssigned(sptChild, agent, idx)) {
                    return true;
                }
            }
        }
        // Did not find the robot in any relevant entry point
        return false;
    }

    public boolean expand(ArrayList<PartialAssignment> fringe, PartialAssignmentPool pool, Assignment old) {
        // iterate next idx for cases of pre-assigned agents:
        while (this.nextAgentIdx < this.assignment.size() && this.assignment.get(this.nextAgentIdx) >= 0) {
            this.nextAgentIdx++;
        }
        if (this.nextAgentIdx >= this.assignment.size()) {
            // No agent left to expand
            return false;
        }
        boolean change = false;
        int numChildren = this.cardinalities.size();

        for (int i = 0; i < numChildren; i++) {

            if (this.cardinalities.get(i).getMax() > 0) {
//                PartialAssignment newPa = pool.getNext();
                // TODO: check  "*newPa = *this;"
//                newPa.copy(this);
//            *newPa = *this;
                PartialAssignment newPa = pool.setNext(this);
                newPa.assignUnassignedAgent(this.nextAgentIdx, i);
                newPa.evaluate(old);

                if (newPa.utility.getMax() > -1.0) {
                    System.out.println("PA: fringe size: " + fringe.size());
                    int index = CommonUtils.upperBound(fringe, newPa, (Comparable<PartialAssignment>) (a, b) -> compare(a, b));
                    fringe.add(index, newPa);
//                    o_container.insert(std::upper_bound (o_container.begin(), o_container.end(), newPa, compare),newPa);
                    change = true;
                }
            }
        }
        return change;
    }


    @Override
    public boolean compareTo(PartialAssignment a, PartialAssignment b) {
        return compare(a, b);
    }

    public int compareTo(PartialAssignment o) {
        boolean compare = compare(this, o);
        return compare ? -1 : 1;
    }

    boolean compare(PartialAssignment a, PartialAssignment b) {
        if (a == b) {
            return false;
        }
        assert (a.getProblem() == b.getProblem());
        long aval = Math.round(a.getUtility().getMax() * PRECISION);
        long bval = Math.round(b.getUtility().getMax() * PRECISION);
        if (aval < bval) {
            // b has higher possible utility
            return false;
        } else if (aval > bval) {
            // a has higher possible utility
            return true;
        }
        // Now we are sure that both partial assignments have the same utility
        else if (a.getPlan().getID() < b.getPlan().getID()) {
            return true;
        } else if (b.getPlan().getID() > b.getPlan().getID()) {
            return false;
        }
        // Now we are sure that both partial assignments have the same utility and the same plan id
        if (a.getAssignedAgentCount() < b.getAssignedAgentCount()) {
            return false;
        } else if (a.getAssignedAgentCount() > b.getAssignedAgentCount()) {
            return true;
        }
        if (a.getUtility().getMin() < b.getUtility().getMin()) {
            // other has higher actual utility
            return false;
        } else if (a.getUtility().getMin() > b.getUtility().getMin()) {
            // this has higher actual utility
            return true;
        }
        for (int i = 0; i < a.assignment.size(); i++) {
            if (a.assignment.get(i) < b.assignment.get(i)) {
                return false;
            } else if (a.assignment.get(i) > b.assignment.get(i)) {
                return true;
            }
        }
        return false;
    }

    public String toString(String pst) {
        String out = "";
        Plan p = this.plan;
        out += pst + "Plan: " + (p != null ? p.getName() : "NULL") + "\n";
        out += pst + "Utility: " + this.utility + "\n";
        out += pst + "Agents: ";
        for (ID agent : this.problem.getAgents()) {
            out += pst + agent + " ";
        }
        out += "\n";
        if (p != null) {
            for (int i = 0; i < this.cardinalities.size() - (this.allowIdling ? 1 : 0); i++) {
                out += pst + "EPid: " + p.getEntryPoints().get(i).getID() + " Task: " + p.getEntryPoints().get(i).getTask().getName()
                        + " cardinality: " + this.cardinalities.get(i);
            }
        }
        out += "\n";
        out += pst + "Assigned Agents: ";
        int i = 0;
        for (int idx : this.assignment) {
            if (idx == -1)
                break;
            out += pst + "  Agent: " + this.problem.getAgents().get(i) + " Ep: " + idx + ", ";
            i++;
        }
        out += "\n";
        return out;
    }

    @Override
    public String toString() {
        String out = "";
        Plan p = this.plan;
        out += "Plan: " + (p != null ? p.getName() : "NULL") + "\n";
        out += "Utility: " + this.utility + "\n";
        out += "Agents: ";
        for (ID agent : this.problem.getAgents()) {
            out += agent + " ";
        }
        out += "\n";
        if (p != null) {
            for (int i = 0; i < this.cardinalities.size() - (this.allowIdling ? 1 : 0); i++) {
                out += "EPid: " + p.getEntryPoints().get(i).getID() + " Task: " + p.getEntryPoints().get(i).getTask().getName()
                        + " cardinality: " + this.cardinalities.get(i);
            }
        }
        out += "\n";
        out += "Assigned Agents: ";
        int i = 0;
        for (int idx : this.assignment) {
            if (idx == -1)
                break;
            out += "  Agent: " + this.problem.getAgents().get(i) + " Ep: " + idx + ", ";
            i++;
        }
        out += "\n";
        return out;
    }

    public Plan getPlan() {
        return this.plan;
    }

    public UtilityInterval getUtility() {
        return this.utility;
    }

    public TaskAssignment getProblem() {
        return this.problem;
    }

    public int getAssignedAgentCount() {
        return this.numAssignedAgents;
    }

    public int getTotalAgentCount() {
        return this.assignment.size();
    }

    public int getEntryPointIndexOf(int agentIdx) {
        return this.assignment.get(agentIdx);
    }

    public int getEntryPointCount() {
        return this.cardinalities.size();
    }

    public void evaluate(Assignment old) {
        this.utility = this.plan.getUtilityFunction().eval(this, old);
    }

    public static void allowIdling(boolean allowed) {
        allowIdling = allowed;
    }

    public static boolean isIdlingAllowed() {
        return allowIdling;
    }



}
