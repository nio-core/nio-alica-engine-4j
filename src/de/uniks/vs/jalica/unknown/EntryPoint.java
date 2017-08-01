package de.uniks.vs.jalica.unknown;

/**
 * Created by alex on 13.07.17.
 */
public class EntryPoint implements Comparable<EntryPoint> {
    private Task task;
    private State state;
    private long id;
    private int maxCardinality;
    private int minCardinality;
    private boolean successRequired;

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
}
