package de.uniks.vs.jalica.behaviours;

import de.uniks.vs.jalica.unknown.AbstractPlan;
import de.uniks.vs.jalica.unknown.AlicaElement;

import java.util.LinkedHashMap;

/**
 * Created by alex on 13.07.17.
 */
public class BehaviourConfiguration extends AbstractPlan {

    private int deferring;
    private int frequency;
    private Behaviour behaviour;
    private boolean eventDriven;
    private LinkedHashMap<String,String> parameters;

    public int getDeferring() {
        return deferring;
    }

    public int getFrequency() {
        return frequency;
    }

    public Behaviour getBehaviour() {
        return behaviour;
    }

    public LinkedHashMap<String, String> getParameters() {
        return parameters;
    }
}
