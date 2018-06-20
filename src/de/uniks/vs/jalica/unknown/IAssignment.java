package de.uniks.vs.jalica.unknown;

import de.uniks.vs.jalica.common.AssignmentCollection;

import java.util.ArrayList;
import java.util.Vector;

/**
 * Created by alex on 17.07.17.
 */
public abstract class IAssignment {

    protected double min;
    protected double max;
    protected Vector<Integer> unassignedAgents = new Vector<>();

    public abstract int getEntryPointCount();
    public abstract ArrayList<Integer> getAgentsWorkingAndFinished(EntryPoint ep);
    public abstract ArrayList<Integer> getUniqueAgentsWorkingAndFinished(EntryPoint ep);
    public abstract AssignmentCollection getEpAgentsMapping();

    public void setMin(double min) {this.min = min; }
    public double getMin() { return min; }
    public void setMax(double max) {this.max = max; }
    public double getMax() { return max; }
    public int getNumUnAssignedRobots() { return unassignedAgents.size(); }
    public Vector<Integer> getUnassignedAgents() {return unassignedAgents;}
}
