package de.uniks.vs.jalica.engine;

import de.uniks.vs.jalica.engine.common.AssignmentCollection;
import de.uniks.vs.jalica.common.utils.CommonUtils;
import de.uniks.vs.jalica.engine.common.StateCollection;
import de.uniks.vs.jalica.engine.containers.messages.AllocationAuthorityInfo;
import de.uniks.vs.jalica.engine.model.EntryPoint;
import de.uniks.vs.jalica.engine.model.Plan;
import de.uniks.vs.jalica.engine.model.State;
import de.uniks.vs.jalica.engine.collections.SuccessCollection;
import de.uniks.vs.jalica.engine.containers.EntryPointAgents;
import de.uniks.vs.jalica.engine.planselection.IAssignment;
import de.uniks.vs.jalica.engine.planselection.PartialAssignment;

import java.util.ArrayList;
import java.util.Set;
import java.util.Vector;

/**
 * Created by alex on 17.07.17.
 */
public class Assignment extends IAssignment {

    private StateCollection agentStateMapping;
    private AssignmentCollection epAgentsMapping;
    private Plan plan;
    private SuccessCollection epSucMapping;

    public Assignment(Plan p) {
        this.plan = p;
        this.max = 0.0;
        this.min = 0.0;

        this.epAgentsMapping = new AssignmentCollection(this.plan.getEntryPoints().size());

        // sort the entrypoints of the given plan
        ArrayList<EntryPoint> sortedEpList = new ArrayList<>();

        for (EntryPoint pair : plan.getEntryPoints().values()) {
            sortedEpList.add(pair);
        }
        sortedEpList.sort(EntryPoint::compareTo);

        // add the sorted entrypoints into the assignmentcollection
//        short i = 0;

        for (int i = 0; i < sortedEpList.size(); i++) { //EntryPoint ep : sortedEpList) {
            this.epAgentsMapping.setEp(i, sortedEpList.get(i));
        }
        this.epAgentsMapping.sortEps();

        this.agentStateMapping = new StateCollection(this.epAgentsMapping);
        this.epSucMapping = new SuccessCollection(p);
    }

    public Assignment(Plan p, AllocationAuthorityInfo aai) {
        this.plan = p;
        this.max = 1;
        this.min = 1;

        this.epAgentsMapping = new AssignmentCollection(p.getEntryPoints().size());

        Vector<Long> curentAgents;
        short i = 0;

        for (EntryPoint epPair : p.getEntryPoints().values()) {

            // set the entrypoint
            if (!this.epAgentsMapping.setEp(i, epPair)) {
                System.err.println(  "Ass: AssignmentCollection Index out of entrypoints bounds!" );
                try {
                    throw new Exception();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            curentAgents = new  Vector<>();
            for (EntryPointAgents epAgents : aai.entryPointAgents) {

                // find the right entrypoint
                if (epAgents.entrypoint == epPair.getID()) {

                    // copy agents
                    for (long agent : epAgents.agents) {
                        curentAgents.add(agent);
                    }

                    // set the agents
                    if (!this.epAgentsMapping.setAgents(i, curentAgents)) {
                        System.err.println("Ass: AssignmentCollection Index out of agents bounds!" );
                        try {
                            throw new Exception();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                }
            }
            i++;
        }

        this.epSucMapping = new SuccessCollection(p);
        this.agentStateMapping = new StateCollection(this.epAgentsMapping);
    }

    public Assignment(PartialAssignment pa) {
        this.max = pa.getMax();
        this.min = max;
        this.plan = pa.getPlan();

        AssignmentCollection assCol = pa.getEpAgentsMapping();

        if (AssignmentCollection.allowIdling) {
            this.epAgentsMapping = new AssignmentCollection(assCol.getSize() - 1);
        }
        else {
            this.epAgentsMapping = new AssignmentCollection(assCol.getSize());
        }

        Vector<Long> curAgents;
        for (short i = 0; i < this.epAgentsMapping.getSize(); i++) {
            // set the entrypoint
            if (!this.epAgentsMapping.setEp(i, assCol.getEntryPoint(i))) {
                CommonUtils.aboutError("Ass: AssignmentCollection Index out of entrypoints bounds!");
            }

            // copy agents
            Vector<Long> agents = assCol.getAgents(i);
            curAgents = new Vector<Long>();

            for (long rob : agents) {
                curAgents.add(rob);
            }

            // set the agents
            if (!this.epAgentsMapping.setAgents(i, curAgents)) {
                CommonUtils.aboutError("Ass: AssignmentCollection Index out of agents bounds!");
            }
        }
        this.agentStateMapping = new StateCollection(this.epAgentsMapping);
        this.epSucMapping = pa.getEpSuccessMapping();

    }

    public void moveAgents(State from, State to) {

        Set<Long> movingAgents = this.agentStateMapping.getAgentsInState(from);
        if (to == null)
        {
            System.out.println("A: MoveAgents is given a State which is NULL!");
        }
        for (long r : movingAgents)
        {
            this.agentStateMapping.setState(r, to);
        }
    }

    public SuccessCollection getEpSuccessMapping() {
        return epSucMapping;
    }

    public boolean removeAgent(long agentID) {

        this.agentStateMapping.removeAgent(agentID);
        Vector<Long> curentAgents;
        for (int i = 0; i < this.epAgentsMapping.getSize(); i++)
        {
            curentAgents = this.epAgentsMapping.getAgents(i);

            //TODO: why is curentAgents size zero?
            if(curentAgents.isEmpty())
                return false;

            int index = CommonUtils.findIndex(curentAgents, 0, curentAgents.size(), agentID);


            if (index > -1)
            {
                curentAgents.remove(index);
                return true;
            }
        }
        return false;
    }

    public boolean removeAgent(long agent, EntryPoint ep) {

        if (ep == null) {
            return false;
        }
        this.agentStateMapping.removeAgent(agent);

        if (this.epAgentsMapping.getAgentsByEp(ep).contains(agent)) {
            this.epAgentsMapping.getAgentsByEp(ep).remove(agent);
            return true;
        }
		else {
            return false;
        }
    }

    public void addAgent(long agentID, EntryPoint entryPoint, State state) {

        if (entryPoint == null) {
            return;
        }
        this.agentStateMapping.setState(agentID, state);

        // TODO: fix -> this.epAgentsMapping.agents to LinkedHashSet
        if (!this.epAgentsMapping.getAgentsByEp(entryPoint).contains(agentID)) {
            this.epAgentsMapping.getAgentsByEp(entryPoint).add(agentID);
        }
        return;
    }

    public void clear() {
        this.agentStateMapping.clear();
        this.epAgentsMapping.clear();
        this.epSucMapping.clear();
    }

    public void setAllToInitialState(ArrayList<Long> agents, EntryPoint defep) {

        for (long r : agents) {
            this.epAgentsMapping.addAgentsByEp(r, defep);
        }

        for (long r : agents) {
            this.agentStateMapping.setState(r, defep.getState());
        }
    }

    public StateCollection getAgentStateMapping() {
        return agentStateMapping;
    }

    public Plan getPlan() {
        return plan;
    }

    public EntryPoint getEntryPointOfAgent(long agent) {

        for (int i = 0; i < this.epAgentsMapping.getSize(); i++) {
            Long iter = CommonUtils.find(this.epAgentsMapping.getAgents(i), 0, this.epAgentsMapping.getAgents(i).size(), Long.valueOf(agent));
//            if (iter != this.epAgentsMapping.getAgents(i).get(this.epAgentsMapping.getAgents(i).size()-1))

            if (iter != null) {
                return this.epAgentsMapping.getEntryPoint(i);
            }
        }
        return null;
    }

    public Vector<Long> getAgentsWorking(long entryPointID) {
        return this.getEpAgentsMapping().getAgentsByID(entryPointID);
    }

//    public Vector<Long> getRobotsWorking(long epid) {
//        return this.getEpAgentsMapping().getAgentsByID(epid);
//    }

    public Vector<Long> getAgentsWorking(EntryPoint ep) {
        return this.getEpAgentsMapping().getAgentsByEp(ep);
    }

//    Vector<Long>  getAgentsWorking(EntryPoint ep) {
//        return this.getEpAgentsMapping().getAgentsByEp(ep);
//    }

    public int totalRobotCount() {
        int c = 0;

        for (int i = 0; i < this.epAgentsMapping.getSize(); i++) {
            c += this.epAgentsMapping.getAgents(i).size();
        }
        return this.getNumUnAssignedRobots() + c;
    }

    public AssignmentCollection getEpAgentsMapping() {
        return epAgentsMapping;
    }

    public boolean updateAgent(long agentID, EntryPoint ep) {
        boolean ret = false;
        for (int i = 0; i < this.epAgentsMapping.getSize(); i++)
        {
            if (this.epAgentsMapping.getEntryPoint(i) == ep)
            {
                if (CommonUtils.find(this.epAgentsMapping.getAgents(i),0, this.epAgentsMapping.getAgents(i).size()-1,
                    agentID) != this.epAgentsMapping.getAgents(i).lastElement())
                {
                    return false;
                }
				else
                {
                    this.epAgentsMapping.getAgents(i).add(agentID);
                    ret = true;
                }
            }
			else
            {
                Long iter = CommonUtils.find(this.epAgentsMapping.getAgents(i), 0, this.epAgentsMapping.getAgents(i).size() - 1, agentID);
                if (iter != this.epAgentsMapping.getAgents(i).lastElement())
                {
                    this.epAgentsMapping.getAgents(i).remove(iter);
                    ret = true;
                }
            }
        }
        if (ret)
        {
            this.agentStateMapping.setState(agentID, ep.getState());
        }
        return ret;
    }

    public Vector<Long> getAllAgents() {
        Vector<Long> ret = new Vector<>();
        for (int i = 0; i < this.epAgentsMapping.getSize(); i++)
        {
            for (int j = 0; j < this.epAgentsMapping.getAgents(i).size(); j++)
            {
                ret.add(this.epAgentsMapping.getAgents(i).get(j));
            }
        }
        return ret;
    }

    public boolean isValid() {
        ArrayList<Long>[] success = this.epSucMapping.getAgents();

        for (int i = 0; i < this.epAgentsMapping.getSize(); i++)
        {
            int c = this.epAgentsMapping.getAgents(i).size() + success[i].size();
            if (c > this.epAgentsMapping.getEntryPoint(i).getMaxCardinality()
                || c < this.epAgentsMapping.getEntryPoint(i).getMinCardinality())
            {
                return false;
            }
        }
        return true;
    }

    public Vector<Long> getUnassignedAgents() {
        return unassignedAgents;
    }


    public double getMax() {
        return max;
    }

    public boolean isSuccessfull() {
        for (int i = 0; i < this.epSucMapping.getCount(); i++)
        {
            if (this.epSucMapping.getEntryPoints()[i].getSuccessRequired())
            {
                if (!(this.epSucMapping.getAgents()[i].size() > 0
                    && this.epSucMapping.getAgents()[i].size()
                    >= this.epSucMapping.getEntryPoints()[i].getMinCardinality()))
                {
                    return false;
                }
            }

        }
        return true;
    }

    @Override
    public int getEntryPointCount() {
        return this.epAgentsMapping.getSize();
    }

    @Override
    public ArrayList<Long> getAgentsWorkingAndFinished(EntryPoint ep) {
        ArrayList<Long> ret = new ArrayList<>();
        Vector<Long> agents = this.epAgentsMapping.getAgentsByEp(ep);

        if (agents != null) {

            for (int i = 0; i < agents.size(); i++) {
                ret.add(agents.get(i));
            }
        }
        ArrayList<Long> succAgents = this.epSucMapping.getAgents(ep);

        if (succAgents != null) {

            for (int i = 0; i < succAgents.size(); i++) {
                ret.add(succAgents.get(i));
            }
        }
        return ret;
    }

    @Override
    public ArrayList<Long> getUniqueAgentsWorkingAndFinished(EntryPoint ep) {
        ArrayList<Long> ret = new ArrayList<>();

        if (this.plan.getEntryPoints().containsKey(ep.getID()))
        {
            Vector<Long> agents = this.epAgentsMapping.getAgentsByEp(ep);

            for (int i = 0; i < agents.size(); i++) {
                ret.add(agents.get(i));
            }

            for (long r : this.epSucMapping.getAgents(ep)) {

                if (CommonUtils.find(ret, 0, ret.size()-1, r) == ret.get(ret.size()-1)) {
                    ret.add(r);
                }
            }
        }
        return ret;
    }

    public boolean updateAgent(long agent, EntryPoint ep, State s) {
        this.agentStateMapping.setState(agent, s);
        boolean ret = false;

        for (int i = 0; i < this.epAgentsMapping.getSize(); i++) {

            if (this.epAgentsMapping.getEntryPoint(i) == ep) {

                if (this.epAgentsMapping.getAgents(i).contains(agent)) {
                    return false;
                }
                else {
                    this.epAgentsMapping.getAgents(i).add(agent);
                    ret = true;
                }
            }
            else
            {
                if ( this.epAgentsMapping.getAgents(i).contains(agent))
                {
                    this.epAgentsMapping.getAgents(i).remove(agent);
                    ret = true;
                }
            }
        }
        return ret;
    }

    @Override
    public void setMin(double min) {
        CommonUtils.aboutNoImpl();
    }

    @Override
    public void setMax(double max) {
        CommonUtils.aboutNoImpl();
    }

    @Override
    public String toString() {
        return "ASS\n" + this.epAgentsMapping.toString();
    }
}
