package de.uniks.vs.jalica.engine;

import de.uniks.vs.jalica.engine.common.Pair;
import de.uniks.vs.jalica.engine.idmanagement.ID;
import de.uniks.vs.jalica.engine.model.State;

import java.util.ArrayList;

//Assignment
public class AgentStatePairs {

    private ArrayList<Pair<ID, State>> data;

    public AgentStatePairs() {
        this.data = new ArrayList<>();
    }

    public State getStateOfAgent(ID id) {

        for (Pair<ID, State> asp : data ) {

            if (asp.fst == id)
                return asp.snd;
        }
        return null;
    }

    public void add(ID id, State state) {
        data.add(new Pair<>(id, state));
    }

    public void reserve(int assignedAgentCount) {
        data.ensureCapacity(assignedAgentCount);
    }

    public ArrayList<Pair<ID, State>> getData() {
        return data;
    }

    public boolean hasAgent(ID id) {

        for (Pair<ID, State> asp : this.data) {

            if (asp.fst == id)
                return true;
        }
        return false;
    }

    public int size() {
        return this.data.size();
    }

    public ArrayList<ID> getAgentsInState(State state) {
        ArrayList<ID> agentIDs = new ArrayList<>();
        for (Pair<ID, State> asp : this.data) {

            if (asp.snd == state)
                agentIDs.add(asp.fst);
        }
        return agentIDs;
    }

    public void clear() {
        this.data.clear();
    }

    public void removeAt(int index) {
        this.data.remove(index);
    }

    public void removeAllIn(ArrayList<ID> agents) {

        for (ID agent : agents) {
            this.data.remove(agent);
        }
    }

    public void remove(ID agent) {
        this.data.remove(agent);
    }

    public void setStateOfAgent(ID id, State state) {

        for (Pair<ID, State> asp: this.data) {

            if (asp.fst == id)
                asp.snd = state;
        }
    }
}
