package de.uniks.vs.jalica.engine.views;

import de.uniks.vs.jalica.engine.AgentStatePairs;
import de.uniks.vs.jalica.engine.Assignment;
import de.uniks.vs.jalica.engine.common.Pair;
import de.uniks.vs.jalica.engine.model.State;

import java.util.ArrayList;

public class AllAgentsView {
    private Assignment assignment;

    public AllAgentsView(Assignment assignment) {
        this.assignment = assignment;
    }

    public ArrayList<Long> get() {
        //TODO : use Lambda
        ArrayList<Long> agents = new ArrayList<>();

        for (int i = 0; i < this.assignment.getEntryPointCount(); i++) {
            AgentStatePairs agentStates = this.assignment.getAgentStates(i);

            for (Pair<Long, State> data : agentStates.getData()){
                agents.add(data.fst);
            }
        }
        return agents;
//            this.assignment.getAgentStates(Idx).getData().get(agentIdx).fst;
    }

    public int size() {
        if (this.assignment == null) {
            return 0;
        }
        int ret = 0;
        for (int i = 0; i < this.assignment.getEntryPointCount(); i++) {
            ret += this.assignment.getAgentStates(i).size();
        }
        return ret;
    }
}
