package de.uniks.vs.jalica.unknown;

import java.util.Vector;

/**
 * Created by alex on 31.07.17.
 */
public class EntryPointAgentPair {
    private final EntryPoint entryPoint;
    private final int agentID;

    public EntryPointAgentPair(EntryPoint ep, int r) {
        this.entryPoint = ep;
        this.agentID = r;
    }

    public  boolean equals( EntryPointAgentPair other) {
        if (other == null)
        {
            return false;
        }
        if (other.entryPoint.getId() != this.entryPoint.getId())
            return false;
        return (other.getAgentID() == this.agentID);
    }

    public int getAgentID() {return agentID;}

    public boolean containedIn(Vector<EntryPointAgentPair> entryPointAgents) {

        for (EntryPointAgentPair entryPointAgentPair: entryPointAgents) {

            if (this.equals(entryPointAgentPair))
                return true;
        }
        return false;
    }

    public EntryPoint getEntryPoint() {return entryPoint;}
}
