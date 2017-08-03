package de.uniks.vs.jalica.unknown;

/**
 * Created by alex on 13.07.17.
 */
public class Transition extends AlicaElement {

    private SyncTransition syncTransition;
    private PreCondition preCondition;
    private State outState;

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
}
