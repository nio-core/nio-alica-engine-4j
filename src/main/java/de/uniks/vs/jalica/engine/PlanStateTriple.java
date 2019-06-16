package de.uniks.vs.jalica.engine;

import de.uniks.vs.jalica.engine.model.AbstractPlan;
import de.uniks.vs.jalica.engine.model.EntryPoint;
import de.uniks.vs.jalica.engine.model.State;

public class PlanStateTriple {

    public State state;
    public EntryPoint entryPoint;
    public AbstractPlan abstractPlan;

    public PlanStateTriple() {}

    PlanStateTriple(AbstractPlan abstractPlan, EntryPoint entryPoint, State state) {
        this.abstractPlan = abstractPlan;
        this.entryPoint = entryPoint;
        this.state = state;
    }

}
