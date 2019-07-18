package de.uniks.vs.jalica.engine.planselection.views;

import de.uniks.vs.jalica.engine.idmanagement.ID;
import de.uniks.vs.jalica.engine.planselection.PartialAssignment;
import de.uniks.vs.jalica.engine.planselection.views.iterators.UniquePartialAssignmentSuccessIterator;

import java.util.ArrayList;
import java.util.Iterator;

public class UniquePartialAssignmentSuccessView extends ArrayList<ID> {

    private final PartialAssignment pas;
    private final int epIdx;


    public UniquePartialAssignmentSuccessView(int epIdx, PartialAssignment pas) {
        this.epIdx = epIdx;
        this.pas = pas;
    }

    UniquePartialAssignmentSuccessIterator begin()  { return new UniquePartialAssignmentSuccessIterator(0, false, this.epIdx, this.pas); }

    UniquePartialAssignmentSuccessIterator end() {
        ArrayList<ID> agents = this.pas.getSuccessData().getAgentsByIndex(this.epIdx);
        return new UniquePartialAssignmentSuccessIterator(agents != null ? agents.size() : 0, true, this.epIdx, this.pas);
    }

    @Override
    public UniquePartialAssignmentSuccessIterator iterator() {
        return begin();
    }

    public int size() { return distance(begin(), end()); }

    private int distance(UniquePartialAssignmentSuccessIterator begin, UniquePartialAssignmentSuccessIterator end) {
        int count = 0;

        while (begin.unequal(end)){
            begin = begin.increase();
            count++;
        }
        return count-1;
    }
}
