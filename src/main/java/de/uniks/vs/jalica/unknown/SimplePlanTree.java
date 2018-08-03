package de.uniks.vs.jalica.unknown;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by alex on 14.07.17.
 */
public class SimplePlanTree {
    /**
     * The parent SimplePlanTree
     */
    private SimplePlanTree parent;
    private HashSet<SimplePlanTree> children = new HashSet<>();
    /**
     * The state occupied by the respective agent.
     */
    private State state;
    private EntryPoint entryPoint;
    /**
     * The id of the agent to which this tree refers to
     */
    private long agentID = -1;
    private boolean newSimplePlanTree;
    /**
     * The timestamp denoting when this tree was received.
     */
    private double receiveTime;
    private ArrayList<Long> stateIds;

    public boolean containsPlan(AbstractPlan plan) {

        if (this.getEntryPoint().getPlan() == plan) {
            return true;
        }
        for (SimplePlanTree spt : this.getChildren()) {
            if (spt.containsPlan(plan)) {
                return true;
            }
        }
        return false;
    }

    public boolean isNewSimplePlanTree() {return newSimplePlanTree;}

    public void setNewSimplePlanTree(boolean newSimplePlanTree) {this.newSimplePlanTree = newSimplePlanTree;}

    public HashSet<SimplePlanTree> getChildren() { return children; }

    public EntryPoint getEntryPoint() {
        return entryPoint;
    }

    public void setEntryPoint(EntryPoint entryPoint) {
        this.entryPoint = entryPoint;
    }

    public State getState() {return state;}

    public void setState(State state) {
        this.state = state;
    }

    public void setStateIds(ArrayList<Long> ids) {
        this.stateIds = ids;
    }

    public ArrayList<Long> getStateIds() {
        return stateIds;
    }

    public void setReceiveTime(double receiveTime) {
        this.receiveTime = receiveTime;
    }

    public double getReceiveTime() {
        return receiveTime;
    }

    public long getAgentID() {return agentID;}

    public void setAgentID(long agentID) {this.agentID = agentID;}
}
