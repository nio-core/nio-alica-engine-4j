package de.uniks.vs.jalica.engine.model;

import de.uniks.vs.jalica.engine.model.AbstractPlan;
import de.uniks.vs.jalica.engine.model.AlicaElement;
import de.uniks.vs.jalica.engine.model.Variable;

/**
 * Created by alex on 03.08.17.
 */
public class VariableBinding extends AlicaElement {

    private Variable var;
    private Variable subVar;
    private AbstractPlan subPlan;

    public AbstractPlan getSubPlan() {
        return subPlan;
    }
    public void setSubPlan(AbstractPlan plan) {
        this.subPlan = plan;
    }

    public Variable getSubVar() {
        return subVar;
    }
    public void setSubVar(Variable subVar) {
        this.subVar = subVar;
    }

    public Variable getVar() {
        return var;
    }
    public void setVar(Variable var) {
        this.var = var;
    }
}
