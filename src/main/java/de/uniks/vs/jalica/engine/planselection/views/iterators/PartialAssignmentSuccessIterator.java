package de.uniks.vs.jalica.engine.planselection.views.iterators;

import de.uniks.vs.jalica.engine.planselection.PartialAssignment;

public class PartialAssignmentSuccessIterator extends PartialAssignmentSuccessIteratorBase {
    public PartialAssignmentSuccessIterator(int idx, boolean successRange, int epIdx, PartialAssignment pas) {
        super(idx, successRange, epIdx, pas);
        toNextValid();
    }

    //operator++
    public PartialAssignmentSuccessIterator increase(){
        ++this.agentIdx;
        toNextValid();
        return this;
    }

    private void toNextValid() {
        if (!this.inSuccessRange) {
            while (agentIdx < pas.getTotalAgentCount() && pas.getEntryPointIndexOf(agentIdx) != epIdx) {
                ++agentIdx;
            }
            if (agentIdx >= pas.getTotalAgentCount()) {
                agentIdx = 0;
                inSuccessRange = true;
            }
        }
    }
}
