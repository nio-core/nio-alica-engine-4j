package de.uniks.vs.jalica.engine.planselection.views.iterators;

import de.uniks.vs.jalica.engine.idmanagement.ID;
import de.uniks.vs.jalica.engine.planselection.PartialAssignment;

import java.util.ArrayList;
import java.util.Iterator;

public class UniquePartialAssignmentSuccessIterator extends PartialAssignmentSuccessIteratorBase {

    public UniquePartialAssignmentSuccessIterator(int idx, boolean successRange, int epIdx,  PartialAssignment pas) {
        super(idx, successRange, epIdx, pas);
        toNextValid();
    }

    // operator++
    public UniquePartialAssignmentSuccessIterator increase(){
        ++this.agentIdx;
        toNextValid();
        return this;
    }

    public void toNextValid() {
        if (!this.inSuccessRange) {
            while (this.agentIdx < this.pas.getTotalAgentCount() && this.pas.getEntryPointIndexOf(this.agentIdx) != this.epIdx) {
                ++this.agentIdx;
            }
            if (this.agentIdx >= this.pas.getTotalAgentCount()) {
                this.agentIdx = 0;
                this.inSuccessRange = true;
            }
        } else {
            ArrayList<ID> successes = this.pas.getSuccessData().getAgentsByIndex(this.epIdx);

            if (successes != null) {

                while (this.agentIdx < successes.size()) {
                    ID id = successes.get(this.agentIdx);
                    PartialAssignmentIterator assignBegin = new PartialAssignmentIterator(0, this.epIdx, this.pas);
                    PartialAssignmentIterator assignEnd = new PartialAssignmentIterator(0, this.pas.getTotalAgentCount(), this.pas);

                    while (assignBegin.value() != id && assignBegin.hasNext()) {
                         assignBegin.increase();
//                        assignBegin = assignBegin.increase();
                    }

                    if (assignBegin.equals(assignEnd)) {
                        break;
                    }

//                    Iterator<ID> iterator = successes.iterator();
//
//                    int count = 0;
//                    while (iterator.hasNext() && count < this.agentIdx) {
//
//                        if (id == iterator.next()) {
//                            break;
//                        }
//                        count++;
//                    }

                    int count = 0;

                    for (; count < this.agentIdx; count++) {

                        if (id == successes.get(count)) {
                            break;
                        }
                    }

                    if (count == this.agentIdx) {
                        break;
                    }
                    ++this.agentIdx;
                }
            }
        }
    }

    int count = 0;
    //TODO: something wrong
    @Override
    public boolean hasNext() {

        if (this.inSuccessRange && (this.pas.getSuccessData().getAgentsByIndex(this.epIdx)).size() > count) {
            count++;
            return true;
        } else if (!this.inSuccessRange && this.pas.getProblem().getAgents().size() > count) {
            count++;
            return true;
        }
        return false;

//        ArrayList<ID> agents = this.pas.getSuccessData().getAgentsByIndex(this.epIdx);
//
//        if(agents != null && agents.size() > (this.agentIdx))
//            return true;
//
//        return false;

//        if (!this.inSuccessRange) {
//
//            if (this.agentIdx < this.pas.getTotalAgentCount() && this.pas.getEntryPointIndexOf(this.agentIdx) != this.epIdx) {
//                return true;
//            }
//        }
//        else {
//
//            ArrayList<ID> successes = this.pas.getSuccessData().getAgentsByIndex(this.epIdx);
//
//            if (successes != null) {
//
//                if (this.agentIdx < successes.size()) {
//                    ID id = successes.get(this.agentIdx);
//                    PartialAssignmentIterator assignBegin = new PartialAssignmentIterator(0, this.epIdx, this.pas);
//                    PartialAssignmentIterator assignEnd = new PartialAssignmentIterator(0, this.pas.getTotalAgentCount(), this.pas);
//
//                    while (assignBegin.value() != id && assignBegin.hasNext()) {
//                        assignBegin.increase();
//                    }
//
//                    if (assignBegin.equals(assignEnd)) {
//                        return false;
//                    }
//                    int count = 0;
//
//                    for (; count < this.agentIdx; count++) {
//
//                        if (id == successes.get(count)) {
//                            break;
//                        }
//                    }
//
//                    if (count == this.agentIdx) {
//                        return false;
//                    }
//                    return true;
//                }
//            }
//        }
//        return false;

//        if (!this.inSuccessRange) {
//
//            if (this.agentIdx < this.pas.getTotalAgentCount() && this.pas.getEntryPointIndexOf(this.agentIdx) != this.epIdx) {
//                return true;
//            }
//        } else {
//            ArrayList<ID> successes = this.pas.getSuccessData().getAgentsByIndex(this.epIdx);
//
//            if (successes != null) {
//
//                while (this.agentIdx < successes.size()) {
//                    ID id = successes.get(this.agentIdx);
//                    PartialAssignmentIterator assignBegin = new PartialAssignmentIterator(0, this.epIdx, this.pas);
//                    PartialAssignmentIterator assignEnd = new PartialAssignmentIterator(0, this.pas.getTotalAgentCount(), this.pas);
//
//                    while (assignBegin.value() != id && assignBegin.hasNext()) {
//                        assignBegin = assignBegin.increase();
//                    }
//                    if (assignBegin.equals(assignEnd)) {
//                        break;
//                    }
//                    Iterator<ID> iterator = successes.iterator();
//
//                    int count = 0;
//                    while (iterator.hasNext() && count < this.agentIdx) {
//
//                        if (id == iterator.next()) {
//                            break;
//                        }
//                        count++;
//                    }
//                    if (count == this.agentIdx) {
//                        break;
//                    }
//                    return true;
//                }
//            }
//        }
//        return false;
    }

    @Override
    public ID next() {
        //TODO: check functionality
        ID value = this.value();
        toNextValid();
        return value;
    }

}
