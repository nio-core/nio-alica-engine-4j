package de.uniks.vs.jalica.unknown;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by alex on 14.07.17.
 */
public class SimplePlanTree {
    /**
     * The parent SimplePlanTree
     */
    private SimplePlanTree parent;
    private HashSet<SimplePlanTree> children;
    /**
     * The state occupied by the respective robot.
     */
    private State state;
    private EntryPoint entryPoint;
    /**
     * The id of the robot to which this tree refers to
     */
    private int robotId = -1;
    private boolean newSimplePlanTree;
    /**
     * The timestamp denoting when this tree was received.
     */
    private long receiveTime;
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

    public HashSet<SimplePlanTree> getChildren() {
        return children;
    }

    public EntryPoint getEntryPoint() {
        return entryPoint;
    }

    public State getState() {return state;}

    public int getRobotId() {return robotId;}

    public void setRobotId(int robotId) {this.robotId = robotId;}
}
