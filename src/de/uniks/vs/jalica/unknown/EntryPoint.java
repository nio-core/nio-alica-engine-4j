package de.uniks.vs.jalica.unknown;

/**
 * Created by alex on 13.07.17.
 */
public class EntryPoint extends AlicaElement implements Comparable<EntryPoint> {
    private Task task;
    private State state;
    private int maxCardinality;
    private int minCardinality;
    private boolean successRequired;
    private Plan plan;

    public Task getTask() {
        return task;
    }

    public State getState() {
        return state;
    }

    public long getId() {
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
        return this.getTask().getId() > o.getTask().getId()?1 : -1;
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
}
