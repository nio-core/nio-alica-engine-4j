package de.uniks.vs.jalica.engine.planselection.views;

import de.uniks.vs.jalica.engine.idmanagement.ID;
import de.uniks.vs.jalica.engine.planselection.PartialAssignment;
import de.uniks.vs.jalica.engine.planselection.views.iterators.PartialAssignmentIterator;

import java.util.ArrayList;
import java.util.Iterator;

public class PartialAssignmentView extends ArrayList<ID> {

    private int epIdx;
    private PartialAssignment pas;

    public PartialAssignmentView(int epIdx, PartialAssignment pas) {
        this.epIdx = epIdx;
        this.pas = pas;
    }
    public PartialAssignmentIterator begin() { return new PartialAssignmentIterator(0, epIdx, pas); }
    public PartialAssignmentIterator end() { return new PartialAssignmentIterator(this.pas.getTotalAgentCount(), epIdx, pas); }
    public int size() { return distance(begin(), end()); }

    private int distance(PartialAssignmentIterator begin, PartialAssignmentIterator end) {
        int count = 0;

        while (begin.unequal(end)){
            begin = begin.increase();
            count++;
        }
        return count-1;
    }

    @Override
    public Iterator<ID> iterator() {
        return begin();
    }


    //    public class PartialAssignmentIterator implements Iterator<Long> {
//
//        private PartialAssignment pas;
//
//        protected int agentIdx;
//        private int epIdx;
//
//        PartialAssignmentIterator(int agentIdx, int epIdx, PartialAssignment pas) {
//            this.pas = pas;
//            this.agentIdx = agentIdx;
//            this.epIdx = epIdx;
//            toNextValid();
//        }
//
//        @Override
//        public boolean hasNext() {
//            return false;
//        }
//
//        @Override
//        public Long next() {
//            return this.value();
//        }
//
//        Long value() {return this.pas.getProblem().getAgents().get(this.agentIdx); }
//
//        PartialAssignmentIterator increase(){
//            ++this.agentIdx;
//            toNextValid();
//            return this;
//        }
//        boolean equals(PartialAssignmentIterator o)  { return this.agentIdx == o.agentIdx; }
//
//        boolean unequal(PartialAssignmentIterator o)  { return !this.equals(o); }
//
//        private void toNextValid() {
//            while (this.agentIdx < this.pas.getTotalAgentCount() && this.pas.getEntryPointIndexOf(this.agentIdx) != this.epIdx) {
//                ++this.agentIdx;
//            }
//        }
//    }

}
