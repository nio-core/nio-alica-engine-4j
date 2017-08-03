package de.uniks.vs.jalica.unknown;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Created by alex on 13.07.17.
 */
public class Plan extends AbstractPlan {

    private Plan name;
    private PostCondition postCondition;
    private LinkedHashMap<Long, EntryPoint> entryPoints;
    private int minCardinality;
    private int maxCardinality;
    private String destinationPath;
    private ArrayList<State> states;
    private ArrayList<FailureState> failureStates;
    private ArrayList<SuccessState> successStates;
    private ArrayList<Transition> transitions;

    public Plan(long id) {
        this.postCondition = null;
        this.id = id;
        this.minCardinality = 0;
        this.maxCardinality = 0;
    }

    public LinkedHashMap<Long, EntryPoint> getEntryPoints() {
        return entryPoints;
    }

    public int getMinCardinality() {
        return minCardinality;
    }

    public int getMaxCardinality() {
        return maxCardinality;
    }

    public void setMinCardinality(int minCardinality) {
        this.minCardinality = minCardinality;
    }

    public void setMaxCardinality(int maxCardinality) {
        this.maxCardinality = maxCardinality;
    }

    public void setDestinationPath(String destinationPath) {
        this.destinationPath = destinationPath;
    }

    public ArrayList<State> getStates() {
        return states;
    }

    public ArrayList<SuccessState> getSuccessStates() {
        return successStates;
    }

    public ArrayList<FailureState> getFailureStates() {
        return failureStates;
    }

    public ArrayList<Transition> getTransitions() {
        return transitions;
    }
}
