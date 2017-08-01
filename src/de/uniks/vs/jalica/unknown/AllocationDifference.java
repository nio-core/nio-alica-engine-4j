package de.uniks.vs.jalica.unknown;

import java.util.Vector;

/**
 * Created by alex on 31.07.17.
 */
public class AllocationDifference {
    private Reason reason;
    private Vector<EntryPointRobotPair> subtractions;
    private Vector<EntryPointRobotPair> additions;

    public Vector<EntryPointRobotPair> getSubtractions() {
        return subtractions;
    }

    public Vector<EntryPointRobotPair> getAdditions() {
        return additions;
    }

    public void setReason(Reason reason) {
        this.reason = reason;
    }

    public enum Reason {message, utility, empty;}
    public void reset() {
        this.additions.clear();
        this.subtractions.clear();
        this.reason = Reason.empty;
    }
}
