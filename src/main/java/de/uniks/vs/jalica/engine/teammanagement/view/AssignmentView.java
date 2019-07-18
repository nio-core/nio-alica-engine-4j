package de.uniks.vs.jalica.engine.teammanagement.view;

import de.uniks.vs.jalica.engine.AgentStatePairs;
import de.uniks.vs.jalica.engine.Assignment;
import de.uniks.vs.jalica.engine.common.Pair;
import de.uniks.vs.jalica.engine.idmanagement.ID;
import de.uniks.vs.jalica.engine.model.State;

import java.util.ArrayList;

public class AssignmentView {

    protected Assignment assignment;
    private int index;

    public AssignmentView() {}

    public AssignmentView(Assignment assignment, int index) {
        this.assignment = assignment;
        this.index = index;
    }

    public ArrayList<ID> get() {
        // TODO : use Lambda
        ArrayList<ID> agents = new ArrayList<>();
        AgentStatePairs agentStates = this.assignment.getAgentStates(index);

        for (Pair<ID, State> data : agentStates.getData()) {
            agents.add(data.fst);
        }
        return agents;
    }
}
