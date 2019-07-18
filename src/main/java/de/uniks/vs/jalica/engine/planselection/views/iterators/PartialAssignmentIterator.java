package de.uniks.vs.jalica.engine.planselection.views.iterators;

import de.uniks.vs.jalica.engine.idmanagement.ID;
import de.uniks.vs.jalica.engine.planselection.PartialAssignment;

import java.util.Iterator;

public class PartialAssignmentIterator  implements Iterator<ID> {

    private PartialAssignment pas;

    private int agentIdx;
    private int epIdx;

    public PartialAssignmentIterator(int agentIdx, int epIdx, PartialAssignment pas) {
        this.pas = pas;
        this.agentIdx = agentIdx;
        this.epIdx = epIdx;
//        toNextValid();
    }

    //TODO: something wrong
    @Override
    public boolean hasNext() {
        boolean b = this.agentIdx < this.pas.getTotalAgentCount();
        boolean b1 = this.pas.getEntryPointIndexOf(this.agentIdx) != this.epIdx;

        if (this.agentIdx < this.pas.getTotalAgentCount() && this.pas.getEntryPointIndexOf(this.agentIdx) != this.epIdx) {
            return true;
        }
        return false;
    }

    @Override
    public ID next() {
        //TODO: check functionality
        ID value = this.value();
        toNextValid();
        return value;
//        toNextValid();
//        return this.value();
    }

    // operator*
    ID value() {return this.pas.getProblem().getAgents().get(this.agentIdx); }

    // operator++
    public PartialAssignmentIterator increase(){
        this.agentIdx++;
        toNextValid();
        return this;
    }

    // operator==
    boolean equals(PartialAssignmentIterator o)  { return this.agentIdx == o.agentIdx; }

    // operator!=
    public boolean unequal(PartialAssignmentIterator o)  { return !this.equals(o); }

    private void toNextValid() {

        while (this.agentIdx < this.pas.getTotalAgentCount() && this.pas.getEntryPointIndexOf(this.agentIdx) != this.epIdx) {
            this.agentIdx++;
        }
    }
}
