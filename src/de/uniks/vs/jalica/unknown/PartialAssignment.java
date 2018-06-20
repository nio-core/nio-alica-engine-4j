package de.uniks.vs.jalica.unknown;

import de.uniks.vs.jalica.common.AssignmentCollection;
import de.uniks.vs.jalica.common.UtilityFunction;

import java.util.ArrayList;
import java.util.Vector;

/**
 * Created by alex on 21.07.17.
 */
public class PartialAssignment extends IAssignment  implements Comparable<PartialAssignment> {

    private static final long PRECISION = 1073741824;
    private static final int INFINIT = Integer.MAX_VALUE;
    private final PartialAssignmentPool pap;

    private long compareVal;
    private boolean hashCalculated;
    private Vector<Integer> agents;
    private Plan plan;
    private UtilityFunction utilFunc;
    private SuccessCollection epSuccessMapping;
    private AssignmentCollection epAgentsMapping;
    private Vector<DynCardinality> dynCardinalities;

    public PartialAssignment(PartialAssignmentPool partialAssignmentPool) {
        this.pap = partialAssignmentPool;
        this.hashCalculated = false;
        this.epAgentsMapping = new AssignmentCollection(AssignmentCollection.maxEpsCount);
        this.unassignedAgents = new Vector<>();
        this.dynCardinalities = new Vector<>(AssignmentCollection.maxEpsCount);
        this.compareVal = PRECISION;

        for (int i = 0; i < AssignmentCollection.maxEpsCount; i++) {
            this.dynCardinalities.add(i, new DynCardinality());
        }
    }

    public static void reset(PartialAssignmentPool partialAssignmentPool) {
        partialAssignmentPool.curentIndex = 0;
    }

    public static PartialAssignment getNew(PartialAssignmentPool pap, PartialAssignment oldPA) {

        if (pap.curentIndex >= pap.maxCount) {
            System.err.println("max PA count reached!");
        }
        PartialAssignment ret = pap.partialAssignments.get(pap.curentIndex++);
        ret.clear();
        ret.min = oldPA.min;
        ret.max = oldPA.max;
        ret.plan = oldPA.plan;
        ret.agents = oldPA.agents;
        ret.utilFunc = oldPA.utilFunc;
        ret.epSuccessMapping = oldPA.epSuccessMapping;

        for (int i = 0; i < oldPA.unassignedAgents.size(); i++) {
            ret.unassignedAgents.add(oldPA.unassignedAgents.get(i));
        }

        for (int i = 0; i < oldPA.dynCardinalities.size(); i++) {
            ret.dynCardinalities.add(i, new DynCardinality(oldPA.dynCardinalities.get(i).getMin(), oldPA.dynCardinalities.get(i).getMax()));
        }

        ret.epAgentsMapping.setSize(oldPA.epAgentsMapping.getSize());

        for (int i = 0; i < oldPA.epAgentsMapping.getSize(); i++) {
            ret.epAgentsMapping.setEp(i, oldPA.epAgentsMapping.getEp(i));

            for (int j = 0; j < oldPA.epAgentsMapping.getAgents(i).size(); j++) {
                ret.epAgentsMapping.getAgents(i).add(oldPA.epAgentsMapping.getAgents(i).get(j));
            }
        }

        return ret;
    }

    public static PartialAssignment getNew(PartialAssignmentPool partialAssignmentPool, Vector<Integer> agents, Plan plan, SuccessCollection successCollection) {

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
            partialAssignment.dynCardinalities.get(i).setMin(partialAssignment.epAgentsMapping.getEp(i).getMinCardinality());
            partialAssignment.dynCardinalities.get(i).setMax(partialAssignment.epAgentsMapping.getEp(i).getMaxCardinality());
            ArrayList<Integer> suc = successCollection.getAgents(partialAssignment.epAgentsMapping.getEp(i));

            if (suc != null) {
                partialAssignment.dynCardinalities.get(i).setMin(partialAssignment.dynCardinalities.get(i).getMin() - suc.size());
                partialAssignment.dynCardinalities.get(i).setMax(partialAssignment.dynCardinalities.get(i).getMax() - suc.size());

                if (partialAssignment.dynCardinalities.get(i).getMin() < 0) {
                    partialAssignment.dynCardinalities.get(i).setMin(0);
                }

                if (partialAssignment.dynCardinalities.get(i).getMax() < 0) {
                    partialAssignment.dynCardinalities.get(i).setMax(0);
                }

//#ifdef SUCDEBUG
                if (CommonUtils.SUCDEBUG_debug)  System.out.println("PA: SuccessCollection" );
                if (CommonUtils.SUCDEBUG_debug)System.out.println( "PA: EntryPoint: " + partialAssignment.epAgentsMapping.getEntryPoints().get(i).toString() );
                if (CommonUtils.SUCDEBUG_debug)System.out.println( "PA: DynMax: " + partialAssignment.dynCardinalities.get(i).getMax() );
                if (CommonUtils.SUCDEBUG_debug)System.out.println( "PA: DynMin: " + partialAssignment.dynCardinalities.get(i).getMin() );
                if (CommonUtils.SUCDEBUG_debug)System.out.print("PA: SucCol: ");

                for (int k : (suc)) {
                    if (CommonUtils.SUCDEBUG_debug)System.out.print( k + ", ");
                }

                if (CommonUtils.SUCDEBUG_debug)System.out.println( "-----------" );
//#endif
            }
        }

        // At the beginning all agents are unassigned
        for (int i : agents)
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
        for (int i = 0; i < this.epAgentsMapping.getSize(); ++i) {

            if (this.dynCardinalities.get(i).getMin() != 0) {
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
        int robot = this.unassignedAgents.get(0);

//        this.unassignedAgents.erase(this.unassignedAgents.begin());
        this.unassignedAgents.remove(0);

        PartialAssignment newPa;

        for (int i = 0; i < this.epAgentsMapping.getSize(); ++i) {

            if (this.dynCardinalities.get(i).getMax() > 0) {
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
        return 0;
    }

    @Override
    public ArrayList<Integer> getAgentsWorkingAndFinished(EntryPoint ep) {
        CommonUtils.aboutNoImpl();
        return null;
    }

    @Override
    public ArrayList<Integer> getUniqueAgentsWorkingAndFinished(EntryPoint ep) {
        CommonUtils.aboutNoImpl();
        return null;
    }

//    @Override
//    public void setMin(double min) {
//        CommonUtils.aboutNoImpl();
//    }
//
//    @Override
//    public void setMax(double max) {
//        CommonUtils.aboutNoImpl();
//    }
//
//    @Override
//    public Vector<Integer> getUnassignedAgents() {
//        CommonUtils.aboutNoImpl();
//        return null;
//    }

    @Override
    public AssignmentCollection getEpAgentsMapping() {
        CommonUtils.aboutNoImpl();
        return null;
    }

    public boolean addIfAlreadyAssigned(SimplePlanTree spt, int agent) {

        if (spt.getEntryPoint().getPlan() == this.plan) {
            EntryPoint curEp;
            int max = this.epAgentsMapping.getSize();

            if (AssignmentCollection.allowIdling) {
                max--;
            }

            for (int i = 0; i < max; ++i) {
                curEp = this.epAgentsMapping.getEp(i);

                if (spt.getEntryPoint().getId() == curEp.getId()) {

                    if (!this.assignAgent(agent, i)) {
                        break;
                    }
                    //remove agent from "To-Add-List"
                    Integer iter = CommonUtils.find(this.unassignedAgents, 0, this.unassignedAgents.size() - 1, agent);

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

    private boolean assignAgent(int agentID, int index) {

        if (this.dynCardinalities.get(index).getMax() > 0) {
            this.epAgentsMapping.getAgents(index).add(agentID);

            if (this.dynCardinalities.get(index).getMin() > 0) {
                this.dynCardinalities.get(index).setMin(this.dynCardinalities.get(index).getMin() - 1);
            }

            if (this.dynCardinalities.get(index).getMax() <= Integer.MAX_VALUE) {
                this.dynCardinalities.get(index).setMax(this.dynCardinalities.get(index).getMax() - 1);
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
        else if (newPa.plan.getId() > this.plan.getId()){
            return -1;
        }else if (newPa.plan.getId() < this.plan.getId()) {
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
        this.max = max;
        this.compareVal = (long)CommonUtils.round(max * PRECISION);
    }
    @Override
    public String toString() {
        String string = "Plan: " + this.plan.getName() + "\n";
        string += "Utility: " + this.min + ".." + this.max + "\n";
        string += "unassignedRobots: ";
        for (int robot : this.unassignedAgents)
        {
            string += robot + " ";
        }
        string += "\n";
        //shared_ptr<vector<EntryPoint*> > ownEps = this.epRobotsMapping.getEntryPoints();
        Vector<Integer> robots;

        for (int i = 0; i < this.epAgentsMapping.getSize(); ++i)
        {
            robots = (this.epAgentsMapping.getAgents(i));
            string += "EPid: " + this.epAgentsMapping.getEp(i).getId() + " Task: " + this.epAgentsMapping.getEp(i).getTask().getName() + " minCar: "
                + this.dynCardinalities.get(i).getMin() + " maxCar: "
                + (this.dynCardinalities.get(i).getMax() == INFINIT ? "*" : String.valueOf(this.dynCardinalities.get(i).getMax())) + " Assigned Robots: ";

            for (int robot : robots) {
                string += robot + " ";
            }
            string += "\n";
        }

        string += this.epAgentsMapping.toString();
        string += "HashCode: " + this.hashCode() + "\n";
        return string;
    }
}
