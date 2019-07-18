package de.uniks.vs.jalica.engine.planselection.views;

import de.uniks.vs.jalica.common.utils.CommonUtils;
import de.uniks.vs.jalica.engine.idmanagement.ID;
import de.uniks.vs.jalica.engine.planselection.PartialAssignment;
import de.uniks.vs.jalica.engine.planselection.views.iterators.PartialAssignmentSuccessIterator;
import de.uniks.vs.jalica.engine.planselection.views.iterators.PartialAssignmentSuccessIteratorBase;

import java.util.ArrayList;

public class PartialAssignmentSuccessView {
    private int epIdx;
    private PartialAssignment pas;

    public PartialAssignmentSuccessView(int epIdx, PartialAssignment partialAssignment) {
        this.epIdx = epIdx;
        this.pas = partialAssignment;
    }

    PartialAssignmentSuccessIterator begin()  { return new PartialAssignmentSuccessIterator(0, false, this.epIdx, this.pas); }

    PartialAssignmentSuccessIterator end() {
            ArrayList<ID> agents = this.pas.getSuccessData().getAgentsByIndex(this.epIdx);
        return new PartialAssignmentSuccessIterator( agents != null ? this.size() : 0, true, this.epIdx, this.pas);
    }

    boolean empty()  { return begin() == end(); }
    public int size()  { return new PartialAssignmentView(this.epIdx, this.pas).size() + (this.pas != null ? this.pas.getSuccessData().getAgentsByIndex(this.epIdx).size() : 0); }

    public boolean isEmpty() {
        return this.empty();
    }

    public ArrayList<Long> get() {
        CommonUtils.aboutImplIncomplete();
        return null;
    }

    public boolean contains(ID value) {

        PartialAssignmentSuccessIterator begin = begin();

        while (begin.value() != value && begin.hasNext()) {
            begin = begin.increase();
        }

        if (begin.equals(end())) {
            return false;
        }

        return true;
    }
}
