package de.uniks.vs.jalica.unknown;

import de.uniks.vs.jalica.engine.AlicaEngine;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Created by alex on 13.07.17.
 */
public class Plan extends AbstractPlan {

    private Plan name;
    private PostCondition postCondition;
    private LinkedHashMap<Long, EntryPoint> entryPoints = new LinkedHashMap<>();
    private int minCardinality;
    private int maxCardinality;
    private String destinationPath;
    private ArrayList<State> states = new ArrayList<>();
    private ArrayList<FailureState> failureStates = new ArrayList<>();
    private ArrayList<SuccessState> successStates = new ArrayList<>();
    private ArrayList<Transition> transitions = new ArrayList<>();
    private ArrayList<SyncTransition> syncTransitions = new ArrayList<>();

    public Plan(long id, AlicaEngine ae) {
        super(ae);
        this.postCondition = null;
        this.id = id;
        this.minCardinality = 0;
        this.maxCardinality = 0;
    }

    EntryPoint getEntryPointTaskID(long taskID)
    {
        for (EntryPoint iter : entryPoints.values())
        {
			 Task task = iter.getTask();
            if (task != null)
            {
                if (task.getId() == taskID)
                {
                    return iter;
                }
            }
            else
            {
                System.out.println("Model: Class Plan: Entrypoint with ID " + iter.getId() + " does not have a Task");
                CommonUtils.aboutError("");
            }
        }
        return null;
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

    public void setPostCondition(PostCondition postCondition) {
        this.postCondition = postCondition;
    }

    public ArrayList<SyncTransition> getSyncTransitions() {
        return syncTransitions;
    }
}
