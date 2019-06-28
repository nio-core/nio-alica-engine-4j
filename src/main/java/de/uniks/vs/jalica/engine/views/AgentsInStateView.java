package de.uniks.vs.jalica.engine.views;

import de.uniks.vs.jalica.engine.AgentStatePairs;
import de.uniks.vs.jalica.engine.Assignment;
import de.uniks.vs.jalica.engine.common.Pair;
import de.uniks.vs.jalica.engine.model.State;

import java.util.ArrayList;

public class AgentsInStateView {
    private Assignment assignment;
    private State state;

    public AgentsInStateView() { }

    public AgentsInStateView(Assignment assignment, State state) {
        this.assignment = assignment;
        this.state = state;
    }

    public ArrayList<Long> get() {
        // TODO: use Lambda
        ArrayList<Long> agents = new ArrayList<>();

        AgentStatePairs agentStates = this.assignment.getAgentStates(this.state.getEntryPoint().getIndex());
        for (Pair<Long, State> data : agentStates.getData()) {
            agents.add(data.fst);
        }
        return agents;
    }
}
