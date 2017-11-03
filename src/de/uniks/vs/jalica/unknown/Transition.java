package de.uniks.vs.jalica.unknown;

/**
 * Created by alex on 13.07.17.
 */
public class Transition extends AlicaElement {

    private SyncTransition syncTransition;
    private PreCondition preCondition;
    private State outState;
    private State inState;

    public SyncTransition getSyncTransition() {
        return syncTransition;
    }

    public boolean evalCondition(RunningPlan r) {
        return this.preCondition.evaluate(r);
    }

    public State getOutState() {
        return outState;
    }

    public PreCondition getPreCondition() {
        return preCondition;
    }

    public void setPreCondition(PreCondition preCondition) {
        this.preCondition = preCondition;
    }

    public void setOutState(State outState) {
        this.outState = outState;
    }

    public void setInState(State inState) {
        this.inState = inState;
    }

    public void setSyncTransition(SyncTransition syncTransition) {
        this.syncTransition = syncTransition;
    }

    public State getInState() {
        return inState;
    }
}
