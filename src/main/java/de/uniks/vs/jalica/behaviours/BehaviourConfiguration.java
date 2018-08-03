package de.uniks.vs.jalica.behaviours;

import de.uniks.vs.jalica.engine.AlicaEngine;
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

    public BehaviourConfiguration(AlicaEngine ae) { super(ae); }

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

    public void setBehaviour(Behaviour behaviour) {
        this.behaviour = behaviour;
    }

    public boolean isEventDriven() {
        return eventDriven;
    }

    public void setEventDriven(boolean eventDriven) {
        this.eventDriven = eventDriven;
    }

    public void setDeferring(int deferring) {
        this.deferring = deferring;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }
}
