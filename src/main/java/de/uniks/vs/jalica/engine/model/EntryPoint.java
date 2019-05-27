package de.uniks.vs.jalica.engine.model;

import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

/**
 * Created by alex on 13.07.17.
 */
public class EntryPoint extends AlicaElement implements Comparable<EntryPoint> {

    public static final long IDLEID = -1;

    private Task task;
    private State state;
    private int maxCardinality;
    private int minCardinality;
    private boolean successRequired;
    private Plan plan;
    private Set<State> reachableStates = new HashSet<>();

    public Task getTask() {
        return task;
    }

    public State getState() {
        return state;
    }

    public long getID() {
        return id;
    }

    public int getMaxCardinality() {
        return maxCardinality;
    }

    public int getMinCardinality() {
        return minCardinality;
    }

    public boolean getSuccessRequired() {
        return successRequired;
    }

    @Override
    public int compareTo(EntryPoint o) {
        return this.getTask().getID() > o.getTask().getID()?1 : -1;
    }

    public void setPlan(Plan plan) {
        this.plan = plan;
    }

    public void setMinCardinality(int minCardinality) {
        this.minCardinality = minCardinality;
    }

    public void setMaxCardinality(int maxCardinality) {
        this.maxCardinality = maxCardinality;
    }

    public void setSuccessRequired(boolean successRequired) {
        this.successRequired = successRequired;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public void setState(State state) {
        this.state = state;
    }

    public void computeReachabilitySet() {
        Vector<State> queue = new Vector<>();
        queue.add(this.state);
        State cs = null;

        while (queue.size() > 0)
        {
            cs = queue.firstElement();
            queue.remove(cs);
            boolean result = this.reachableStates.add(cs);
            if (result)
            {
                for (Transition t : cs.getOutTransitions())
                {
                    queue.add(t.getOutState());
                }
            }

        }
    }

    public AbstractPlan getPlan() {return plan;}

    public Set<State> getReachableStates() {return reachableStates;}
}
