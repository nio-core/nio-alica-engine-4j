package de.uniks.vs.jalica.engine.model;

import de.uniks.vs.jalica.engine.UtilityFunction;
import de.uniks.vs.jalica.engine.AlicaEngine;

import java.util.ArrayList;

/**
 * Created by alex on 13.07.17.
 * Updated 22.6.19
 */
public class Plan extends AbstractPlan {

    private int minCardinality;
    private int maxCardinality;
    private ArrayList<EntryPoint> entryPoints;
    private ArrayList<State> states;
    private ArrayList<SuccessState> successStates;
    private ArrayList<FailureState> failureStates;
    private ArrayList<Synchronisation> synchronisations;
    private ArrayList<Transition> transitions;
    // TODO: change shared to unique ptr (this requires a change to autogeneration templates)
    private UtilityFunction utilityFunction;
    private double utilityThreshold;
    private boolean masterPlan;
    private RuntimeCondition runtimeCondition;
    private PreCondition preCondition;

//    private String destinationPath;
//    private PostCondition postCondition;
//    private ArrayList<SyncTransition> syncTransitions = new ArrayList<>();

    public Plan(AlicaEngine ae, long id) {
        super(ae, id);
        this.minCardinality = 0;
        this.maxCardinality = 0;
        this.masterPlan = false;
        this.utilityFunction = null;
        this.utilityThreshold = 1.0;
        this.runtimeCondition = null;
        this.preCondition = null;

        this.entryPoints = new ArrayList<>();
        this.states = new ArrayList<>();
        this.successStates = new ArrayList<>();
        this.failureStates = new ArrayList<>();
        this.synchronisations = new ArrayList<>();
        this.transitions = new ArrayList<>();
    }

    public EntryPoint getEntryPointTaskID(long taskID) {

        for ( EntryPoint ep : this.entryPoints) {
            Task task = ep.getTask();
            assert(task != null);

            if (task.getID() == taskID) {
                return ep;
            }
        }
        return null;
    }

    public EntryPoint getEntryPointByID(long epID) {

        for (EntryPoint ep : this.entryPoints) {

            if (ep.getID() == epID) {
                return ep;
            }
        }
        return null;
    }

    public State getStateByID(long stateID) {

        for ( State s : this.states) {

            if (s.getID() == id) {
                return s;
            }
        }
        return null;
    }

    public ArrayList<EntryPoint> getEntryPoints() {
        return this.entryPoints;
    }

    public ArrayList<State> getStates() {
        return this.states;
    }

    public ArrayList<FailureState> getFailureStates() {
        return this.failureStates;
    }

    public ArrayList<SuccessState> getSuccessStates() {
        return this.successStates;
    }

    public int getMaxCardinality() {
        return this.maxCardinality;
    }

    public int getMinCardinality() {
        return this.minCardinality;
    }

    public boolean isMasterPlan() {
        return this.masterPlan;
    }

    public UtilityFunction getUtilityFunction() {
        return this.utilityFunction;
    }

    public double getUtilityThreshold() {
        return this.utilityThreshold;
    }

    public ArrayList<Transition> getTransitions() {
        return this.transitions;
    }

    public ArrayList<Synchronisation> getSynchronisations() {
        return this.synchronisations;
    }

    public RuntimeCondition getRuntimeCondition() {
        return this.runtimeCondition;
    }

    public PreCondition getPreCondition() {
        return this.preCondition;
    }

    public void setUtilityThreshold(double utilityThreshold) {
        this.utilityThreshold = utilityThreshold;
    }

    public void setUtilityFunction(UtilityFunction utilityFunction) {
        this.utilityFunction = utilityFunction;
    }

    public void setRuntimeCondition(RuntimeCondition runtimeCondition) {
        this.runtimeCondition = runtimeCondition;
    }

    private void setEntryPoints(ArrayList<EntryPoint> entryPoints) {
        this.entryPoints = entryPoints;
    }

    private void setFailureStates( ArrayList<FailureState> failureStates) {
        this.failureStates = failureStates;
    }

    public void setMaxCardinality(int maxCardinality) {
        this.maxCardinality = maxCardinality;
    }

    public void setMinCardinality(int minCardinality) {
        this.minCardinality = minCardinality;
    }

    public void setMasterPlan(boolean masterPlan) {
        this.masterPlan = masterPlan;
    }

    private void setStates( ArrayList<State> states) {
        this.states = states;
    }

    private void setSuccessStates( ArrayList<SuccessState> successStates) {
        this.successStates = successStates;
    }

    private void setSynchronisations( ArrayList<Synchronisation> synchronisations) {
        this.synchronisations = synchronisations;
    }

    public void setPreCondition(PreCondition preCondition) {
        this.preCondition = preCondition;
    }

    private void setTransitions(ArrayList<Transition> transitions) {
        this.transitions = transitions;
    }

    @Override
    public String toString() {
        String indent = "";
        String ss = "";
        ss += indent + "#Plan: " + super.toString();
        ss += indent + "\tIsMasterPlan: " + this.masterPlan + "\n";
        ss += indent + "\tUtility Threshold: " + this.utilityThreshold + "\n";
        if (this.preCondition != null) {
            ss += this.preCondition.toString();
        }
        if (this.runtimeCondition != null) {
            ss += this.runtimeCondition.toString();
        }
        for (EntryPoint ep : this.entryPoints) {
            ss += ep.toString() + "\t";
        }
        for (State state : this.states) {
            ss += state.toString() + "\t";
        }
        for (Variable var : this.getVariables()) {
            ss += var.toString() + "\t";
        }

        ss += indent + "#EndPlan:" + "\n";
        return ss;
    }
}
