package de.uniks.vs.jalica.engine.model;

import de.uniks.vs.jalica.engine.RunningPlan;
import de.uniks.vs.jalica.engine.common.SyncTransition;

/**
 * Created by alex on 13.07.17.
 * Updated 22.6.19
 */
public class Transition extends AlicaElement {

    protected PreCondition preCondition;
    protected State inState;
    protected State outState;
    protected Synchronisation synchronisation;

    public Transition() {
        this.preCondition = null;
        this.inState = null;
        this.outState = null;
        this.synchronisation = null;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    public State getOutState()  { return this.outState; }
    public State getInState()  { return this.inState; }
    public Synchronisation getSynchronisation()  { return this.synchronisation; }
    public PreCondition getPreCondition()  { return this.preCondition; }

    public boolean evalCondition( RunningPlan r) {
        return this.preCondition.evaluate(r);
    }

    public void setPreCondition(PreCondition preCondition) {
        this.preCondition = preCondition;
    }

    public void setInState(State inState) {
        this.inState = inState;
    }

    public void setOutState(State outState) {
        this.outState = outState;
    }

    public void setSynchronisation(Synchronisation synchronisation) {
        this.synchronisation = synchronisation;
    }
}
