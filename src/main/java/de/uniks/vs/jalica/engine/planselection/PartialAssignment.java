package de.uniks.vs.jalica.engine.planselection;

import de.uniks.vs.jalica.engine.common.AssignmentCollection;
import de.uniks.vs.jalica.engine.UtilityFunction;
import de.uniks.vs.jalica.engine.SimplePlanTree;
import de.uniks.vs.jalica.engine.model.EntryPoint;
import de.uniks.vs.jalica.engine.model.Plan;
import de.uniks.vs.jalica.engine.collections.SuccessCollection;
import de.uniks.vs.jalica.engine.common.CommonUtils;
import de.uniks.vs.jalica.engine.common.DynCardinality;

import java.util.ArrayList;
import java.util.Vector;

/**
 * Created by alex on 21.07.17.
 */
public class PartialAssignment extends IAssignment implements Comparable<PartialAssignment> {

    private static final long PRECISION = 1073741824;
    private static final int INFINIT = Integer.MAX_VALUE;
    private final PartialAssignmentPool pap;

    private long compareVal;
    private boolean hashCalculated;
    private Vector<Long> agents;
    private Plan plan;
    private UtilityFunction utilFunc;
    private SuccessCollection epSuccessMapping;
    private AssignmentCollection epAgentsMapping;
    private DynCardinality[] dynCardinalities;

    public PartialAssignment(PartialAssignmentPool partialAssignmentPool) {
        this.pap = partialAssignmentPool;
        this.hashCalculated = false;
        this.epAgentsMapping = new AssignmentCollection(AssignmentCollection.maxEpsCount);
        this.unassignedAgents = new Vector<>();
        this.dynCardinalities = new DynCardinality[AssignmentCollection.maxEpsCount];
        this.compareVal = PRECISION;

        for (int i = 0; i < AssignmentCollection.maxEpsCount; i++) {
            this.dynCardinalities[i] = new DynCardinality();
        }
    }

    public static void reset(PartialAssignmentPool partialAssignmentPool) {
        partialAssignmentPool.curentIndex = 0;
    }

    public static PartialAssignment getNew(PartialAssignmentPool partialAssignmentPool, PartialAssignment oldPartialAssignment) {
        if (partialAssignmentPool.curentIndex >= partialAssignmentPool.maxCount) {
            System.err.println("max PA count reached!");
        }
        PartialAssignment newPartialAssignment = partialAssignmentPool.partialAssignments.get(partialAssignmentPool.curentIndex++);
        newPartialAssignment.clear();
        newPartialAssignment.min = oldPartialAssignment.min;
        newPartialAssignment.max = oldPartialAssignment.max;
        newPartialAssignment.plan = oldPartialAssignment.plan;
        newPartialAssignment.agents = oldPartialAssignment.agents;
        newPartialAssignment.utilFunc = oldPartialAssignment.utilFunc;
        newPartialAssignment.epSuccessMapping = oldPartialAssignment.epSuccessMapping;

        for (int i = 0; i < oldPartialAssignment.unassignedAgents.size(); i++) {
            newPartialAssignment.unassignedAgents.add(oldPartialAssignment.unassignedAgents.get(i));
        }

        for (int i = 0; i < oldPartialAssignment.dynCardinalities.length; i++) {
            newPartialAssignment.dynCardinalities[i] = new DynCardinality(oldPartialAssignment.dynCardinalities[i].getMin(), oldPartialAssignment.dynCardinalities[i].getMax());
        }

        newPartialAssignment.epAgentsMapping.setSize(oldPartialAssignment.epAgentsMapping.getSize());

        for (int i = 0; i < oldPartialAssignment.epAgentsMapping.getSize(); i++) {
            newPartialAssignment.epAgentsMapping.setEp(i, oldPartialAssignment.epAgentsMapping.getEntryPoint(i));

            for (int j = 0; j < oldPartialAssignment.epAgentsMapping.getAgents(i).size(); j++) {
                newPartialAssignment.epAgentsMapping.getAgents(i).add(oldPartialAssignment.epAgentsMapping.getAgents(i).get(j));
            }
        }

        return newPartialAssignment;
    }

    public static PartialAssignment getNew(PartialAssignmentPool partialAssignmentPool, Vector<Long> agents, Plan plan, SuccessCollection successCollection) {

        if (partialAssignmentPool.curentIndex >= partialAssignmentPool.maxCount) {
            System.out.println( "PA: max PA count reached!" );
        }
        PartialAssignment partialAssignment = partialAssignmentPool.partialAssignments.get(partialAssignmentPool.curentIndex++);
        partialAssignment.clear();
        partialAssignment.agents = agents; // Should already be sorted! (look at TaskAssignment, or PlanSelector)
        partialAssignment.plan = plan;
        partialAssignment.utilFunc = plan.getUtilityFunction();
        partialAssignment.epSuccessMapping = successCollection;
        // Create EP-Array

        if (AssignmentCollection.allowIdling) {
            partialAssignment.epAgentsMapping.setSize(plan.getEntryPoints().size() + 1);
            // Insert IDLE-EntryPoint
            partialAssignment.epAgentsMapping.setEp(partialAssignment.epAgentsMapping.getSize() - 1, partialAssignmentPool.idleEntryPoint);
        }
        else {
            partialAssignment.epAgentsMapping.setSize(plan.getEntryPoints().size());
        }
        // Insert plan entrypoints
        int j = 0;

        for ( Long key : plan.getEntryPoints().keySet()) {
            partialAssignment.epAgentsMapping.setEp(j++, plan.getEntryPoints().get(key));
        }
        // Sort the entrypoint array
        partialAssignment.epAgentsMapping.sortEps();

        for (int i = 0; i < partialAssignment.epAgentsMapping.getSize(); i++) {
            partialAssignment.dynCardinalities[i].setMin(partialAssignment.epAgentsMapping.getEntryPoint(i).getMinCardinality());
            partialAssignment.dynCardinalities[i].setMax(partialAssignment.epAgentsMapping.getEntryPoint(i).getMaxCardinality());
            ArrayList<Long> suc = successCollection.getAgents(partialAssignment.epAgentsMapping.getEntryPoint(i));

            if (suc != null) {
                partialAssignment.dynCardinalities[i].setMin(partialAssignment.dynCardinalities[i].getMin() - suc.size());
                partialAssignment.dynCardinalities[i].setMax(partialAssignment.dynCardinalities[i].getMax() - suc.size());

                if (partialAssignment.dynCardinalities[i].getMin() < 0) {
                    partialAssignment.dynCardinalities[i].setMin(0);
                }

                if (partialAssignment.dynCardinalities[i].getMax() < 0) {
                    partialAssignment.dynCardinalities[i].setMax(0);
                }

//#ifdef SUCDEBUG
                if (CommonUtils.SUCDEBUG_debug) {
                    System.out.println("PA: SuccessCollection");
                    System.out.println("PA: EntryPoint: " + partialAssignment.epAgentsMapping.getEntryPoints()[i].getName());
                    System.out.println("PA: DynMax: " + partialAssignment.dynCardinalities[i].getMax());
                    System.out.println("PA: DynMin: " + partialAssignment.dynCardinalities[i].getMin());
                    System.out.print("PA: SucCol: ");

                    for (long k : (suc)) {
                        System.out.print(k + ", ");
                    }
                    System.out.println("-----------");
                }
//#endif
            }
        }

        // At the beginning all agents are unassigned
        for (long i : agents)
        {
            partialAssignment.unassignedAgents.add(i);
        }
        return partialAssignment;
    }

    private void clear() {
        this.min = 0.0;
        this.max = 1.0;

        this.compareVal = PRECISION;
        this.unassignedAgents.clear();

        for (int i = 0; i < this.epAgentsMapping.getSize(); i++) {
           this.epAgentsMapping.getAgents(i).clear();
        }
        this.hashCalculated = false;
    }

    public boolean isGoal() {
        // There should be no unassigned robots anymore
        if (this.unassignedAgents.size() > 0) {
            return false;
        }
        // Every EntryPoint should be satisfied according to his minCar
        for (int i = 0; i < this.epAgentsMapping.getSize(); i++) {

            if (CommonUtils.PA_DEBUG_debug) System.out.println("PA:  Nr "+ i +" - " + this.dynCardinalities[i].getMin());

            if (this.dynCardinalities[i].getMin() != 0) {
                System.out.println("PA:  PartialAssignment is not a complete Assignment " + this.dynCardinalities[i].getMin());
                return false;
            }
        }
        return true;
    }

    public ArrayList<PartialAssignment> expand() {
        ArrayList<PartialAssignment> newPas = new ArrayList<PartialAssignment>();
        if (this.unassignedAgents.size() == 0) {
            // No robot left to expand
            return newPas;
        }
        // Robot which should be assigned next
        long robot = this.unassignedAgents.get(0);

//        this.unassignedAgents.erase(this.unassignedAgents.begin());
        this.unassignedAgents.remove(0);
        PartialAssignment newPa = null;

        for (int i = 0; i < this.epAgentsMapping.getSize(); ++i) {


            if (this.dynCardinalities[i].getMax() > 0) {
                // Update the cardinalities and assign the robot
                newPa = PartialAssignment.getNew(pap, this);
                newPa.assignAgent(robot, i);
                newPas.add(newPa);
            }

        }

        return newPas;
    }

    @Override
    public int getEntryPointCount() {
        return this.epAgentsMapping.getSize();
    }

    @Override
    public ArrayList<Long> getAgentsWorkingAndFinished(EntryPoint ep) {
        ArrayList<Long> ret = new ArrayList<>();
        Vector<Long> robots = this.epAgentsMapping.getAgentsByEp(ep);

        if (robots != null) {

            for ( long id : robots){
                ret.add(id);
            }
        }
        ArrayList<Long> successes = this.epSuccessMapping.getAgents(ep);

        if (successes != null) {

            for (long id : (successes)){
                ret.add(id);
            }
        }
        return ret;
    }

    @Override
    public ArrayList<Long> getUniqueAgentsWorkingAndFinished(EntryPoint ep) {
        ArrayList<Long> ret = new ArrayList<>();
        Vector<Long> robots = this.epAgentsMapping.getAgentsByEp(ep);

        for ( long id : (robots)) {
            ret.add(id);
        }

        ArrayList<Long> successes = this.epSuccessMapping.getAgents(ep);

        if (successes != null) {

            for (long success : (successes)) {

//                if (find(ret.begin(), ret.end(), iter) == ret.end()) {
                if (!ret.contains(success)) {
                    ret.add(success);
                }
            }
        }
        return ret;
    }


    @Override
    public AssignmentCollection getEpAgentsMapping() {
        return epAgentsMapping;
    }

    public boolean addIfAlreadyAssigned(SimplePlanTree spt, long agent) {

        if (spt.getEntryPoint().getPlan() == this.plan) {
            EntryPoint curEp;
            int max = this.epAgentsMapping.getSize();

            if (AssignmentCollection.allowIdling) {
                max--;
            }

            for (int i = 0; i < max; ++i) {
                curEp = this.epAgentsMapping.getEntryPoint(i);

                if (spt.getEntryPoint().getID() == curEp.getID()) {

                    if (!this.assignAgent(agent, i)) {
                        break;
                    }
                    //remove agent from "To-Add-List"
                    Long iter = CommonUtils.find(this.unassignedAgents, 0, this.unassignedAgents.size() - 1, agent);

                    if (this.unassignedAgents.remove(this.unassignedAgents.indexOf(iter)) == this.unassignedAgents.lastElement()) {
                        System.err.println( "PA: Tried to assign agent " + agent + ", but it was NOT UNassigned!");
                        try {
                            throw new Exception();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    //return true, because we are ready, when we found the agent here
                    return true;
                }
            }
            return false;
        }
        // If there are children and we didnt find the agent until now, then go on recursive
		else if (spt.getChildren().size() > 0) {

            for (SimplePlanTree sptChild : spt.getChildren()) {

                if (this.addIfAlreadyAssigned(sptChild, agent)) {
                    return true;
                }
            }
        }
        // Did not find the agent in any relevant entry point
        return false;
    }

    private boolean assignAgent(long agentID, int index) {

        if (this.dynCardinalities[index].getMax() > 0) {
            this.epAgentsMapping.getAgents(index).add(agentID);

            if (this.dynCardinalities[index].getMin() > 0) {
                this.dynCardinalities[index].setMin(this.dynCardinalities[index].getMin() - 1);
            }

            if (this.dynCardinalities[index].getMax() <= Integer.MAX_VALUE) {
                this.dynCardinalities[index].setMax(this.dynCardinalities[index].getMax() - 1);
            }
            return true;
        }
        return false;
    }

    public UtilityFunction getUtilFunc() { return utilFunc; }

    public Plan getPlan() { return this.plan; }

    public SuccessCollection getEpSuccessMapping() { return epSuccessMapping; }

    @Override
    public int compareTo(PartialAssignment newPa) {
        //TODO has perhaps to be changed
        // 0 , -1 = false
        // 1 true
        if (this == newPa) { // Same reference . same object
            return 0;
        }
        if (newPa.compareVal < this.compareVal) {
            // other has higher possible utility
            return 0;
        }
        else if (newPa.compareVal > this.compareVal) {
            // this has higher possible utility
            return 1;
        }
        // Now we are sure that both partial assignments have the same utility
        else if (newPa.plan.getID() > this.plan.getID()){
            return -1;
        }else if (newPa.plan.getID() < this.plan.getID()) {
            return 0;
        }
        // Now we are sure that both partial assignments have the same utility and the same plan id
        if (this.unassignedAgents.size() < newPa.unassignedAgents.size()) {
            return 0;
        }
        else if (this.unassignedAgents.size() > newPa.unassignedAgents.size()) {
            return 1;
        }
        if (newPa.min < this.min) {
            // other has higher actual utility
            return 0;
        }
        else if (newPa.min > this.min){
            // this has higher actual utility
            return -1;
        }

        for (int i = 0; i < this.epAgentsMapping.getSize(); ++i) {

            if (this.epAgentsMapping.getAgents(i).size() < newPa.epAgentsMapping.getAgents(i).size()) {
                return 0;
            }
			else if (this.epAgentsMapping.getAgents(i).size() < newPa.epAgentsMapping.getAgents(i).size()) {
                return -1;
            }
        }
        for (int i = 0; i <= this.epAgentsMapping.getSize(); ++i) {

            for (int j = 0; j < this.epAgentsMapping.getAgents(i).size(); ++j) {

                if (this.epAgentsMapping.getAgents(i).get(j) > newPa.epAgentsMapping.getAgents(i).get(j)) {
                    return 0;
                }
				else if (this.epAgentsMapping.getAgents(i).get(j) > newPa.epAgentsMapping.getAgents(i).get(j)) {
                    return 1;
                }
            }
        }
        return -1;
    }

    @Override
    public void setMax(double max) {
        super.setMax(max);
        this.compareVal = (long)CommonUtils.round(max * PRECISION);
    }
    @Override
    public String toString() {
        String string = "Plan: " + this.plan.getName() + "\n";
        string += "Utility: " + this.min + ".." + this.max + "\n";
        string += "unassignedAgents: ";

        for (long agentID : this.unassignedAgents) {
            string += agentID + " ";
        }
        string += "\n";
        //shared_ptr<vector<EntryPoint*> > ownEps = this.epRobotsMapping.getEntryPoints();
        Vector<Long> agents;

        for (int i = 0; i < this.epAgentsMapping.getSize(); i++) {
            agents = (this.epAgentsMapping.getAgents(i));
            string += "EPid: " + this.epAgentsMapping.getEntryPoint(i).getID() + " Task: " + this.epAgentsMapping.getEntryPoint(i).getTask().getName() + " minCar: "
                + this.dynCardinalities[i].getMin() + " maxCar: "
                + (this.dynCardinalities[i].getMax() == INFINIT ? "*" : String.valueOf(this.dynCardinalities[i].getMax())) + " Assigned Agents: ";

            for (long agent : agents) {
                string += agent + " ";
            }
            string += "\n";
        }

        string += this.epAgentsMapping.toString();
        string += "HashCode: " + this.hashCode() + "\n";
        return string;
    }


    @Override
    public Vector<Long> getAgentsWorking(long entryPoint) {
        return this.epAgentsMapping.getAgentsByID(entryPoint);
    }

    @Override
    public Vector<Long> getAgentsWorking(EntryPoint entryPoint) {
        return this.epAgentsMapping.getAgentsByEp(entryPoint);

    }
}
