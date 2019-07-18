package de.uniks.vs.jalica.engine.planselection.views.iterators;

import de.uniks.vs.jalica.common.utils.CommonUtils;
import de.uniks.vs.jalica.engine.idmanagement.ID;
import de.uniks.vs.jalica.engine.planselection.PartialAssignment;

import java.util.Iterator;

public class PartialAssignmentSuccessIteratorBase implements Iterator<ID> {

    protected PartialAssignment pas;
    protected int agentIdx;
    protected int epIdx;
    protected boolean inSuccessRange;

    PartialAssignmentSuccessIteratorBase(int idx, boolean successRange, int epIdx, PartialAssignment pas) {
        this.pas = pas;
        this.epIdx = epIdx;
        this.agentIdx = idx;
        this.inSuccessRange = successRange;
    }

    // operator*()
    public ID value() {
        if (this.inSuccessRange) {
            return (this.pas.getSuccessData().getAgentsByIndex(this.epIdx)).get(this.agentIdx);
        } else {
            return this.pas.getProblem().getAgents().get(this.agentIdx);
        }
    }

    // operator==
    boolean equals( PartialAssignmentSuccessIteratorBase o)  { return this.agentIdx == o.agentIdx && this.inSuccessRange == o.inSuccessRange; }

    // operator!=
    public boolean unequal(PartialAssignmentSuccessIteratorBase o)  { return !(this == o); }


    // TODO: move hasNext and next methods from inherited classes
    @Override
    public boolean hasNext() {
        CommonUtils.aboutImplIncomplete();
        return false;
    }

    @Override
    public ID next() {
        CommonUtils.aboutImplIncomplete();
        return null;
    }
}
