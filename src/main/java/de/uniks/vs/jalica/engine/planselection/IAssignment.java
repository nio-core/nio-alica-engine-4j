package de.uniks.vs.jalica.engine.planselection;

import de.uniks.vs.jalica.engine.common.AssignmentCollection;
import de.uniks.vs.jalica.engine.model.EntryPoint;

import java.util.ArrayList;
import java.util.Vector;

/**
 * Created by alex on 17.07.17.
 */
public abstract class IAssignment {

    protected double min = 0;
    protected double max = 1;
    protected Vector<Long> unassignedAgents = new Vector<>();

    public abstract int getEntryPointCount();
    public abstract ArrayList<Long> getAgentsWorkingAndFinished(EntryPoint ep);
    public abstract ArrayList<Long> getUniqueAgentsWorkingAndFinished(EntryPoint ep);
    public abstract Vector<Long> getAgentsWorking(long entryPoint);
    public abstract Vector<Long> getAgentsWorking(EntryPoint entryPoint);
    public abstract AssignmentCollection getEpAgentsMapping();

    public void setMin(double min) {this.min = min; }
    public double getMin() { return min; }
    public void setMax(double max) {this.max = max; }
    public double getMax() { return max; }
    public int getNumUnAssignedRobots() { return unassignedAgents.size(); }
    public Vector<Long> getUnassignedAgents() {return unassignedAgents;}
}
