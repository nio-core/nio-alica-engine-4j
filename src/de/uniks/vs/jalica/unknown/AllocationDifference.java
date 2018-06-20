package de.uniks.vs.jalica.unknown;

import java.util.Vector;

/**
 * Created by alex on 31.07.17.
 */
public class AllocationDifference {
    private Reason reason;
    private Vector<EntryPointAgentPair> subtractions = new Vector<>();
    private Vector<EntryPointAgentPair> additions = new Vector<>();

    public AllocationDifference() {
        this.reason = Reason.empty;
    }

    public Vector<EntryPointAgentPair> getSubtractions() {
        return subtractions;
    }

    public Vector<EntryPointAgentPair> getAdditions() {
        return additions;
    }

    public void setReason(Reason reason) {
        this.reason = reason;
    }

    public Reason getReason() {return reason;}

    public void applyDifference(AllocationDifference other) {

        for (EntryPointAgentPair pair: other.additions) {

            for (EntryPointAgentPair otherPair: this.subtractions) {

                if (pair.equals(otherPair)) {
                    this.subtractions.remove(otherPair);
                }
                else if (!pair.containedIn(this.additions)) {
                    this.additions.add(pair);
                }
            }
        }

        for (EntryPointAgentPair pair: other.subtractions) {

            for (EntryPointAgentPair otherPair: this.additions) {

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
        String string = "Additions: ";

        for (int i = 0; i < this.additions.size(); i++) {
            string += "+ " + this.additions.get(i).getAgentID() + " (" + this.additions.get(i).getEntryPoint().getId() + ")";
        }
        string += "\n" + "Substractions: ";

        for (int i = 0; i < this.subtractions.size(); i++) {
            string += "- " + this.subtractions.get(i).getAgentID() + " ("
                + this.subtractions.get(i).getEntryPoint().getId() + ")";
        }
        string += "\n" + "Reason [0=message, 1=utility, 2=empty]:" + this.reason.ordinal();
        return string;
    }
}
