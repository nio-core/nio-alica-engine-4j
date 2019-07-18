package de.uniks.vs.jalica.engine.model;

import de.uniks.vs.jalica.engine.common.DynCardinality;
import de.uniks.vs.jalica.engine.idmanagement.ID;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

/**
 * Created by alex on 13.07.17.
 * Updated 23.6.19
 */
public class EntryPoint extends AlicaElement implements Comparable<EntryPoint> {

    public static final long IDLEID = -1;
    public static String IDLENAME = "IDLE-EP";

    private State state;
    private Task task;
    private Plan plan;
    private DynCardinality cardinality;
    private boolean successRequired;
    private int index;
    private Set<State> reachableStates = new HashSet<>();

    public static EntryPoint generateIdleEntryPoint() {
        EntryPoint idleEP = new EntryPoint();
        idleEP.setName(EntryPoint.IDLENAME);
        idleEP.setID(EntryPoint.IDLEID);
        idleEP.index = -42;
        idleEP.cardinality = new DynCardinality(0, Integer.MAX_VALUE);
        Task idleTask = new Task();
        idleTask.setName(Task.IDLENAME);
        idleTask.setID(Task.IDLEID);
        idleEP.setTask(idleTask);
        return idleEP;
    }


    public EntryPoint() {
        this.task = null;
        this.state = null;
        cardinality = new DynCardinality(0, 0);
        this.plan = null;
        successRequired = false;
        index = -1;
    }

    public EntryPoint(long id, Plan p, Task t, State s) {
        super(id);
        task = t;
        state = s;
        successRequired = false;
        plan = p;
        cardinality = new DynCardinality(0, 0);
        index = -1;
    }

    public void computeReachabilitySet() {
        ArrayList< State> queue = new ArrayList<>();
        queue.add(0, this.state);
        State cs = null;

        while (!queue.isEmpty()) {
            cs = queue.get(0);
            queue.remove(0);

            if (!this.reachableStates.contains(cs)) {
                this.reachableStates.add(cs);

                for ( Transition t : cs.getOutTransitions()) {
                    queue.add(t.getOutState());
                }
            }
        }
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public void setState(State state) {
        this.state = state;
    }

    public void setPlan(Plan plan) {
        this.plan = plan;
    }

    public void setSuccessRequired(boolean successRequired) {
        this.successRequired = successRequired;
    }


    @Override
    public int compareTo(EntryPoint o) {
        return this.getTask().getID() > o.getTask().getID()?1 : -1;
    }

    public Task getTask() {
        return task;
    }

    public AbstractPlan getPlan() {return plan;}
    public State getState() {
        return state;
    }

    public int getMaxCardinality()  { return this.cardinality.getMax(); }
    public int getMinCardinality()  { return this.cardinality.getMin(); }

    public DynCardinality getCardinality() {
        return cardinality;
    }

    public boolean isSuccessRequired()  { return this.successRequired; }

    public Set<State> getReachableStates() {return reachableStates;}
    public boolean isStateReachable(State s) {
        return this.reachableStates.contains(s);
    }

    public int getIndex() { return this.index; }
    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public String toString() {
        String indent = "";
        String ss = "";
        ss += indent + "#EntryPoint: Name: " + getName() + " ID: " + getID() + "\n";
        ss += indent + "\t Cardinality: " + this.cardinality + "\n";
        ss += indent + "\t Task: ";
        if (this.task != null) {
            ss += "Name: " + this.task.getName() + " ID: " +  this.task.getID() + "\n";
        } else {
            ss += "null" + "\n";
        }
        ss += indent + "\t Initial State: ";
        if (this.state != null) {
            ss += "Name: " + this.state.getName() + " ID: " + this.state.getID() + "\n";
        } else {
            ss += "null" + "\n";
        }
        ss += indent + "#EndEntryPoint" + "\n";
        return ss;
    }
}
