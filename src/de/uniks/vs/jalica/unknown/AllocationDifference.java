package de.uniks.vs.jalica.unknown;

import java.util.Vector;

/**
 * Created by alex on 31.07.17.
 */
public class AllocationDifference {
    private Reason reason;
    private Vector<EntryPointRobotPair> subtractions = new Vector<>();
    private Vector<EntryPointRobotPair> additions = new Vector<>();

    public AllocationDifference() {
        this.reason = Reason.empty;
    }

    public Vector<EntryPointRobotPair> getSubtractions() {
        return subtractions;
    }

    public Vector<EntryPointRobotPair> getAdditions() {
        return additions;
    }

    public void setReason(Reason reason) {
        this.reason = reason;
    }

    public Reason getReason() {return reason;}

    public void applyDifference(AllocationDifference other) {

        for (EntryPointRobotPair pair: other.additions) {

            for (EntryPointRobotPair otherPair: this.subtractions) {

                if (pair.equals(otherPair)) {
                    this.subtractions.remove(otherPair);
                }
                else if (!pair.containedIn(this.additions)) {
                    this.additions.add(pair);
                }
            }
        }

        for (EntryPointRobotPair pair: other.subtractions) {

            for (EntryPointRobotPair otherPair: this.additions) {

                if (pair.equals(otherPair)) {
                    this.additions.remove(otherPair);
                }
                else if (!pair.containedIn(this.subtractions)) {
                    this.subtractions.add(pair);
                }
            }
        }
    }

    public boolean isEmpty() {
        return this.additions.size() == 0 && this.subtractions.size() == 0;
    }

    public enum Reason {message, utility, empty;}

    public void reset() {
        this.additions.clear();
        this.subtractions.clear();
        this.reason = Reason.empty;
    }

    @Override
    public String toString() {
        String ss = "Additions: ";

        for (int i = 0; i < this.additions.size(); i++) {
            ss += "+ " + this.additions.get(i).getRobot() + " (" + this.additions.get(i).getEntryPoint().getId() + ")";
        }
        ss += "\n" + "Substractions: ";

        for (int i = 0; i < this.subtractions.size(); i++) {
            ss += "- " + this.subtractions.get(i).getRobot() + " ("
                + this.subtractions.get(i).getEntryPoint().getId() + ")";
        }
        ss += "\n" + "Reason [0=message, 1=utility, 2=empty]:" + this.reason.ordinal();
        return ss;
    }
}
