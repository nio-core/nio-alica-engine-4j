package de.uniks.vs.jalica.engine;

import de.uniks.vs.jalica.engine.idmanagement.ID;
import de.uniks.vs.jalica.engine.model.AbstractPlan;
import de.uniks.vs.jalica.engine.model.EntryPoint;
import de.uniks.vs.jalica.engine.model.State;

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
     * The id of the agent teamObserver which this tree refers teamObserver
     */
    private ID agentID;
    private boolean isNew = true;
    /**
     * The timestamp denoting when this tree was received.
     */
    private AlicaTime receiveTime;
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

    public boolean isNewSimplePlanTree() {return isNew;}

    public void setNewSimplePlanTree(boolean aNew) {this.isNew = aNew;}

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

    public void setReceiveTime(AlicaTime receiveTime) {
        this.receiveTime = receiveTime;
    }

    public AlicaTime getReceiveTime() {
        return receiveTime;
    }

    public ID getAgentID() {return agentID;}

    public void setAgentID(ID agentID) {this.agentID = agentID;}

    public void setProcessed() { this.isNew = false; }

    public SimplePlanTree getParent() {
        return parent;
    }

    public void setParent(SimplePlanTree parent) {
        this.parent = parent;
    }


    public String toString(String pst) {
        String out = "";
        out += pst+ "RobotID: " + this.agentID + "\n";
        out += pst+ "Parent: ";

        if (this.parent != null) {
            out += pst+ this.parent.getState().getID();
        }
        out += "\n";
        out += pst+ "State: ";

        if (this.state != null) {
            out += pst+ this.state.getID();
            out += pst+ "    " + this.state.getName();
        } else {
            out += pst+ "ERROR !!!NO STATE !!!";
        }
        out += "\n";
        out += pst+ "EntryPoint: ";

        if (this.entryPoint != null) {
            out += pst+ this.entryPoint.getID() + " " + this.entryPoint.getTask().getName();
        } else {
            out += pst+ "NoEntryPoint";
        }
        out += "\n";
        out += pst+ "Children: " + this.children.size() + "\n";

        for ( SimplePlanTree c : this.children) {
            out += pst+ c;
        }
        out += "\n";
        return out;
    }

    @Override
    public String toString() {
        String out = "";
        out += "RobotID: " + this.agentID + "\n";
        out += "Parent: ";

        if (this.parent != null) {
            out += this.parent.getState().getID();
        }
        out += "\n";

        out += "State: ";
        if (this.state != null) {
            out += this.state.getID();
            out += " " + this.state.getName();
        } else {
            out += "ERROR !!!NO STATE !!!";
        }

        out += "\n";

        out += "EntryPoint: ";

        if (this.entryPoint != null) {
            out += this.entryPoint.getID() + " " + this.entryPoint.getTask().getName();
        } else {
            out += "NoEntryPoint";
        }
        out += "\n";

        out += "Children: " + this.children.size() + "\n";
        for ( SimplePlanTree c : this.children) {
            out += c;
        }

        out += "\n";

        return out;
    }
}
