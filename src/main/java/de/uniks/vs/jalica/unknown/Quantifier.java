package de.uniks.vs.jalica.unknown;

import de.uniks.vs.jalica.engine.AlicaEngine;

import java.util.ArrayList;

/**
 * Created by alex on 13.07.17.
 */
public class Quantifier extends AlicaElement {
    private ArrayList<String> domainIdentifiers = new ArrayList<>();
    boolean scopeIsEntryPoint;
    /**
     * Indicates that the scope of this quantifier is a Plan
     */
    boolean scopeIsPlan;
    /**
     * Indicates that the scope of this quantifier is an State
     */
    boolean scopeIsState;
    EntryPoint entryPoint;
    State state;
    Plan plan;

    public ArrayList<String> getDomainIdentifiers() {
        return domainIdentifiers;
    }

    public void setScope(AlicaEngine a, AlicaElement ae) {

        if (ae instanceof Plan)
        {
            this.plan = (Plan)ae;
        }
        else if (ae instanceof  EntryPoint)
        {
            this.entryPoint = (EntryPoint)ae;
        }
        else if (ae instanceof  State)
        {
            this.state = (State)ae;
        }
        else
        {
            a.abort("Scope of Quantifier is not an entrypoint, plan, or state: ", ""+ae);
        }
    }
}
