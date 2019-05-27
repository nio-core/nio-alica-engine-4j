package de.uniks.vs.jalica.engine.model;

import de.uniks.vs.jalica.engine.RunningPlan;
import de.uniks.vs.jalica.engine.common.SyncTransition;

/**
 * Created by alex on 13.07.17.
 */
public class Transition extends AlicaElement {

    protected SyncTransition syncTransition;
    protected PreCondition preCondition;
    protected State outState;
    protected State inState;

    public SyncTransition getSyncTransition() {
        return syncTransition;
    }

    public boolean evalCondition(RunningPlan r) {

        if(this.preCondition == null)
            return false;

        return this.preCondition.evaluate(r);
    }


    public PreCondition getPreCondition() {
        return preCondition;
    }

    public void setPreCondition(PreCondition preCondition) {

        if (this.preCondition == preCondition )
            return;
        PreCondition tmpPreCondition = this.preCondition;
        this.preCondition = preCondition;

        if(tmpPreCondition != null)
            tmpPreCondition.setTransition(this);
    }

    public State getOutState() {
        return outState;
    }
    public void setOutState(State outState) {

        if(this.outState == outState)
            return;
        State tmpState = this.outState;
        this.outState = outState;

        if(tmpState != null)
            tmpState.removeOutTransition(this);
    }
    public void deleteOutState(State state) {

        if (this.outState == null || this.outState != state)
            return;
        this.outState = null;
        state.removeOutTransition(this);
    }

    public void setSyncTransition(SyncTransition syncTransition) {
        this.syncTransition = syncTransition;
    }

    public State getInState() {
        return inState;
    }
    public void setInState(State inState) {

        if(this.inState == inState)
            return;
        State tmpState = this.inState;
        this.inState = inState;

        if(tmpState != null)
            tmpState.removeInTransition(this);
    }
    public void deleteInState(State state) {

        if (this.inState == null || this.inState != state)
            return;
        this.inState = null;
        state.removeInTransition(this);
    }
}
