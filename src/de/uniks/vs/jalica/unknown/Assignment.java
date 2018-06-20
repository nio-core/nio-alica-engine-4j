package de.uniks.vs.jalica.unknown;

import de.uniks.vs.jalica.common.AssignmentCollection;

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
        for (EntryPoint pair : plan.getEntryPoints().values())
        {
            sortedEpList.add(pair);
        }
        sortedEpList.sort(EntryPoint::compareTo);

        // add the sorted entrypoints into the assignmentcollection
        short i = 0;
        for (EntryPoint ep : sortedEpList)
        {
            this.epAgentsMapping.setEp(i++, ep);
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

        Vector<Integer> curentAgents;
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

            curentAgents = new  Vector<Integer>();
            for (EntryPointAgents epAgents : aai.entryPointAgents) {

                // find the right entrypoint
                if (epAgents.entrypoint == epPair.getId()) {

                    // copy agents
                    for (int agent : epAgents.agents) {
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

        Vector<Integer> curAgents;
        for (short i = 0; i < this.epAgentsMapping.getSize(); i++) {
            // set the entrypoint
            if (!this.epAgentsMapping.setEp(i, assCol.getEp(i))) {
                CommonUtils.aboutError("Ass: AssignmentCollection Index out of entrypoints bounds!");
            }

            // copy agents
            Vector<Integer> agents = assCol.getAgents(i);
            curAgents = new Vector<Integer>();

            for (int rob : agents) {
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

        Set<Integer> movingAgents = this.agentStateMapping.getAgentsInState(from);
        if (to == null)
        {
            System.out.println("A: MoveAgents is given a State which is NULL!");
        }
        for (int r : movingAgents)
        {
            this.agentStateMapping.setState(r, to);
        }
    }

    public SuccessCollection getEpSuccessMapping() {
        return epSucMapping;
    }

    public boolean removeAgent(int agentID) {

        this.agentStateMapping.removeAgent(agentID);
        Vector<Integer> curentAgents;
        for (int i = 0; i < this.epAgentsMapping.getSize(); i++)
        {
            curentAgents = this.epAgentsMapping.getAgents(i);

            //TODO: why is curentAgents size zero?
            if(curentAgents.isEmpty())
                return false;

            int newAgentID = CommonUtils.find(curentAgents, 0, curentAgents.size() - 1, agentID);

            if (newAgentID != curentAgents.size()-1)
            {
                curentAgents.remove(newAgentID);
                return true;
            }
        }
        return false;
    }

    public boolean removeAgent(int agent, EntryPoint ep) {

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

    public void addAgent(int id, EntryPoint e, State s) {
        if (e == null)
        {
            return;
        }
        this.agentStateMapping.setState(id, s);
        this.epAgentsMapping.getAgentsByEp(e).add(id);
        return;
    }

    public void clear() {
        this.agentStateMapping.clear();
        this.epAgentsMapping.clear();
        this.epSucMapping.clear();
    }

    public void setAllToInitialState(ArrayList<Integer> agents, EntryPoint defep) {
        Vector<Integer> rlist = this.epAgentsMapping.getAgentsByEp(defep);
        for (int r : agents)
        {
            rlist.add(r);
        }
        for (int r : agents)
        {
            this.agentStateMapping.setState(r, defep.getState());
        }
    }

    public StateCollection getAgentStateMapping() {
        return agentStateMapping;
    }

    public Plan getPlan() {
        return plan;
    }

    public EntryPoint getEntryPointOfAgent(int agent) {

        for (int i = 0; i < this.epAgentsMapping.getSize(); i++)
        {
            Integer iter = CommonUtils.find(this.epAgentsMapping.getAgents(i), 0, this.epAgentsMapping.getAgents(i).size() - 1,
                    Integer.valueOf(agent));
            if (iter != this.epAgentsMapping.getAgents(i).get(this.epAgentsMapping.getAgents(i).size()-1))
            {
                return this.epAgentsMapping.getEp(i);
            }
        }
        return null;
    }

    public Vector<Integer> getAgentsWorking(EntryPoint ep) {
        return this.getEpAgentsMapping().getAgentsByEp(ep);
    }

    public AssignmentCollection getEpAgentsMapping() {
        return epAgentsMapping;
    }

    public boolean updateAgent(int agentID, EntryPoint ep) {
        boolean ret = false;
        for (int i = 0; i < this.epAgentsMapping.getSize(); i++)
        {
            if (this.epAgentsMapping.getEp(i) == ep)
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
                Integer iter = CommonUtils.find(this.epAgentsMapping.getAgents(i), 0, this.epAgentsMapping.getAgents(i).size() - 1, agentID);
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

    public Vector<Integer> getAllAgents() {
        Vector<Integer> ret = new Vector<Integer>();
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
        Vector<ArrayList<Integer>> success = this.epSucMapping.getAgents();

        for (int i = 0; i < this.epAgentsMapping.getSize(); ++i)
        {
            int c = this.epAgentsMapping.getAgents(i).size() + success.get(i).size();
            if (c > this.epAgentsMapping.getEp(i).getMaxCardinality()
                || c < this.epAgentsMapping.getEp(i).getMinCardinality())
            {
                return false;
            }
        }
        return true;
    }

    public Vector<Integer> getUnassignedAgents() {
        return unassignedAgents;
    }


    public double getMax() {
        return max;
    }

    public boolean isSuccessfull() {
        for (int i = 0; i < this.epSucMapping.getCount(); i++)
        {
            if (this.epSucMapping.getEntryPoints().get(i).getSuccessRequired())
            {
                if (!(this.epSucMapping.getAgents().get(i).size() > 0
                    && this.epSucMapping.getAgents().get(i).size()
                    >= this.epSucMapping.getEntryPoints().get(i).getMinCardinality()))
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
    public ArrayList<Integer> getAgentsWorkingAndFinished(EntryPoint ep) {
        CommonUtils.aboutNoImpl();
        return null;
    }

    @Override
    public ArrayList<Integer> getUniqueAgentsWorkingAndFinished(EntryPoint ep) {
        ArrayList<Integer>  ret = new ArrayList<>();

        if (this.plan.getEntryPoints().containsKey(ep.getId()))
        {
            Vector<Integer> agents = this.epAgentsMapping.getAgentsByEp(ep);

            for (int i = 0; i < agents.size(); i++) {
                ret.add(agents.get(i));
            }

            for (Integer r : this.epSucMapping.getAgents(ep)) {

                if (CommonUtils.find(ret, 0, ret.size()-1, r) == ret.get(ret.size()-1)) {
                    ret.add(r);
                }
            }
        }
        return ret;
    }

    public boolean updateAgent(int agent, EntryPoint ep, State s) {
        this.agentStateMapping.setState(agent, s);
        boolean ret = false;

        for (int i = 0; i < this.epAgentsMapping.getSize(); i++) {

            if (this.epAgentsMapping.getEp(i) == ep) {

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
