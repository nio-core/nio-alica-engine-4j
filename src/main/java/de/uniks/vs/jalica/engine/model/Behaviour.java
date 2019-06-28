package de.uniks.vs.jalica.engine.model;

import de.uniks.vs.jalica.common.utils.CommonUtils;
import de.uniks.vs.jalica.engine.BasicBehaviour;
import de.uniks.vs.jalica.engine.AlicaEngine;
import de.uniks.vs.jalica.engine.teammanagement.Agent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by alex on 13.07.17.
 * update 21.6.19
 */
public class Behaviour extends AbstractPlan {

    /**
     * Specifies whether this Behaviour is run eventDriven. If it is not event driven, a timer will call it according to
     * Frequency and Deferring.
     */
    private boolean eventDriven;
    /**
     * The frequency with which this Behaviour is called in case it is not EventDriven.
     */
    private int frequency;
    /**
     * The time in ms to wait before this Behaviour is executed for the first time after entering the corresponding
     * state. Has only effect for Behaviours not running in EventDriven mode.
     */
    private int deferring;
    private RuntimeCondition runtimeCondition;
    private PreCondition preCondition;
    private PostCondition postCondition;
    /**
     * The set of static parameters of this Behaviour configuration. Usually parsed by
     * BasicBehaviour.InitializeParameters.
     */
    private HashMap<String, String> parameters;

    public Behaviour(AlicaEngine ae) {
        super(ae);
        this.preCondition = null;
        this.runtimeCondition = null;
        this.postCondition = null;
        this.frequency = 1;
        this.deferring = 0;
        this.eventDriven = false;
        this.parameters = new HashMap<>();
    }

    public int getDeferring()  { return this.deferring; }
    public boolean isEventDriven()  { return this.eventDriven; }
    public int getFrequency()  { return this.frequency; }
    public RuntimeCondition getRuntimeCondition()  { return this.runtimeCondition; }
    public PreCondition getPreCondition()  { return this.preCondition; }
    public PostCondition getPostCondition()  { return this.postCondition; }
    public HashMap<String, String> getParameters()  { return this.parameters; }

    public void setDeferring(int deferring) {
        this.deferring = deferring;
    }
    void setEventDriven(boolean eventDriven){
        this.eventDriven = eventDriven;
    }
    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    @Override
    public String toString() {
        String indent = "";
        String ss = "";
        ss += indent + "#Behaviour: " + getName() + " " + this.getID() + "\n";
        ss += indent + "\teventDriven: " + (this.eventDriven ? "true" : "false") + "\n";
        ss += indent + "\tfrequency: " + this.frequency + "\n";
        ss += indent + "\tdeferring: " + this.deferring + "\n";
        if (this.preCondition != null) {
            ss += this.preCondition.toString();
        }
        if (this.runtimeCondition != null) {
            ss += this.runtimeCondition.toString();
        }
        if (this.postCondition != null) {
            ss += this.postCondition.toString();
        }
        ss += indent + "\tparameters: " + "\n";
        for (Map.Entry<String, String> entry : this.parameters.entrySet()) {
            ss += indent + "\t" + entry.getKey() + " = " + entry.getValue() + "\n";
        }

        ss += indent + "#EndBehaviour" + "\n";
        return ss;
    }

    public void setRuntimeCondition(RuntimeCondition runtimeCondition) {
        this.runtimeCondition = runtimeCondition;
    }

    public void setPreCondition(PreCondition preCondition) {
        this.preCondition = preCondition;
    }

    public void setPostCondition(PostCondition postCondition) {
        this.postCondition = postCondition;
    }

    public void setParameters(HashMap<String, String> parameters) {
        this.parameters = parameters;
    }

    public boolean isFinished() {
        CommonUtils.aboutImplIncomplete("reimplement (see old behaviour version)");
        return false;
    }
}
