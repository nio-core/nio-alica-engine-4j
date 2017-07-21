package de.uniks.vs.jalica.behaviours;

import de.uniks.vs.jalica.unknown.AbstractPlan;
import de.uniks.vs.jalica.unknown.AlicaElement;

/**
 * Created by alex on 13.07.17.
 */
public class BehaviourConfiguration extends AbstractPlan {

    private Object parameters;
    private Object variables;
    private Object deferring;
    private int frequency;
    private Behaviour behaviour;

    public Object getParameters() {
        return parameters;
    }

    public Object getVariables() {
        return variables;
    }

    public Object getDeferring() {
        return deferring;
    }

    public int getFrequency() {
        return frequency;
    }

    public Behaviour getBehaviour() {
        return behaviour;
    }
}
