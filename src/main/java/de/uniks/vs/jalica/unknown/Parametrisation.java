package de.uniks.vs.jalica.unknown;

/**
 * Created by alex on 03.08.17.
 */
public class Parametrisation extends AlicaElement {

    private Variable var;
    private Variable subVar;
    private AbstractPlan subPlan;

    public void setSubPlan(AbstractPlan plan) {
        this.subPlan = plan;
    }

    public void setSubVar(Variable subVar) {
        this.subVar = subVar;
    }

    public void setVar(Variable var) {
        this.var = var;
    }
}
