package de.uniks.vs.jalica.engine.views;

import de.uniks.vs.jalica.engine.AgentStatePairs;
import de.uniks.vs.jalica.engine.Assignment;
import de.uniks.vs.jalica.engine.common.Pair;
import de.uniks.vs.jalica.engine.model.State;

import java.util.ArrayList;

public class AssignmentSuccessView {
    private boolean inSuccess;
    private Assignment assignment;
    private int epIdx;

    public AssignmentSuccessView(Assignment assignment, int index) {
        this.assignment = assignment;
        this.epIdx = index;

//            if (!inSuccess && agentStates != null && this.index >= agentStates.size()) {
//                this.inSuccess = true;
//                this.index = 0;
//            }
//        return AssignmentSuccessIterator(0, false, &_assignment->getAgentStates(_epIdx), &_assignment->getSuccessData(_epIdx));
//        return AssignmentSuccessIterator(_assignment->getSuccessData(_epIdx).size(), true, &_assignment->getAgentStates(_epIdx), &_assignment->getSuccessData(_epIdx));
    }

    public int size() {
        return this.assignment != null ? (this.assignment.getAgentStates(this.epIdx).size() + this.assignment.getSuccessData(this.epIdx).size()) : 0;
    }

    public ArrayList<Long> get() {
        ArrayList<Long> agents = new ArrayList<>();
        agents.addAll(this.assignment.getSuccessData(this.epIdx));

        for (Pair<Long, State> agent : this.assignment.getAgentStates(this.epIdx).getData()) {
            agents.add(agent.fst);
        }
        return agents;
    }
}
