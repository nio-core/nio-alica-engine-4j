package de.uniks.vs.jalica.engine.authority;

import de.uniks.vs.jalica.engine.idmanagement.ID;
import de.uniks.vs.jalica.engine.model.EntryPoint;

import java.util.Vector;

/**
 * Created by alex on 31.07.17.
 */
public class EntryPointAgentPair {
    private final EntryPoint entryPoint;
    private final ID agentID;

    public EntryPointAgentPair(EntryPoint ep, ID r) {
        this.entryPoint = ep;
        this.agentID = r;
    }

    public  boolean equals( EntryPointAgentPair other) {
        if (other == null)
        {
            return false;
        }
        if (other.entryPoint.getID() != this.entryPoint.getID())
            return false;
        return (other.getAgentID() == this.agentID);
    }

    public ID getAgentID() {return agentID;}

    public boolean containedIn(Vector<EntryPointAgentPair> entryPointAgents) {

        for (EntryPointAgentPair entryPointAgentPair: entryPointAgents) {

            if (this.equals(entryPointAgentPair))
                return true;
        }
        return false;
    }

    public EntryPoint getEntryPoint() {return entryPoint;}
}
