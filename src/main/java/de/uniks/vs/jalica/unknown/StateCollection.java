package de.uniks.vs.jalica.unknown;

import de.uniks.vs.jalica.common.AssignmentCollection;

import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

/**
 * Created by alex on 17.07.17.
 */
public class StateCollection {

    private Vector<Long> agents = new Vector<>();
    private Vector<State> states = new Vector<>();

    public StateCollection(AssignmentCollection ac) {

        for(int i = 0;i < ac.getSize(); i ++) {
            State initialState = ac.getEntryPoint(i).getState();

            for(long r : ac.getAgents(i)) {
                this.setState(r, initialState);
            }
        }
    }

    public Set<Long> getAgentsInState(State state) {
//        if (CommonUtils.SC_DEBUG_debug)
            System.out.println("SC: agent size:" + agents.size());
        Set<Long> stateAgents = new HashSet<>();

        for (int i = 0; i < this.agents.size(); i++) {

            if (this.states.get(i) == state) {
                stateAgents.add(this.agents.get(i));
            }
        }
        if (CommonUtils.SC_DEBUG_debug) System.out.println("SC: agents in state:" + stateAgents.size());
        return stateAgents;
    }

    public void setStates(Vector<State> states) {
        this.states = states;
    }


    public void setState(long agent, State state) {

        for (int i = 0; i < this.agents.size(); i++) {

            if (this.agents.get(i) == agent) {
                this.states.set(i, state);
                return;
            }
        }
        this.agents.add(agent);
        this.states.add(state);
    }

    public void removeAgent(long agentID) {

        for(int i = 0; i < this.states.size(); i++) {

            if(this.agents.get(i) == agentID) {
                this.agents.remove( i);
                this.states.remove( i);
                return;
            }
        }
    }

    public void clear() {
        this.agents.clear();
        this.states.clear();
    }

    public State getStateOfAgent(long agentID) {

        for (int i = 0; i < this.agents.size(); i++) {

            if (this.agents.get(i) == agentID) {
                return this.states.get(i);
            }
        }
        return null;
    }

    public void setStates(Vector<Long> agentIDs, State state) {

        for(int i = 0; i <  agentIDs.size(); i++) {
            setState(agentIDs.get(i), state);
        }
    }

    public State getState(long agentID) {

        for (int i = 0; i < this.agents.size(); i++) {

            if (this.agents.get(i) == agentID) {
                return this.states.get(i);
            }
        }
        return null;
    }

    public void reconsiderOldAssignment(Assignment oldOne, Assignment newOne) {

        if(oldOne.getPlan() != newOne.getPlan()) {
            return;
        }
        //shared_ptr<vector<EntryPoint*> >eps = oldOne.getEntryPoints();
        EntryPoint ep;
        for(short i = 0; i < oldOne.getEntryPointCount(); i++)
        {
            ep = oldOne.getEpAgentsMapping().getEntryPoint(i);
            for(long rid : (oldOne.getAgentsWorking(ep)))
            {
                Long iter = CommonUtils.find(newOne.getAgentsWorking(ep), 0, newOne.getAgentsWorking(ep).size() - 1, rid);
                if(iter != newOne.getAgentsWorking(ep).lastElement())
                {
                    this.setState(rid, oldOne.getAgentStateMapping().getState(rid));
                }
            }
        }
    }
}
