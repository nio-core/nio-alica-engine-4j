package de.uniks.vs.jalica.unknown;

import java.util.LinkedHashMap;

/**
 * Created by alex on 13.07.17.
 */
public class Plan extends AbstractPlan {
    private Plan name;
    private LinkedHashMap<Long, EntryPoint> entryPoints;
    private int minCardinality;

    public LinkedHashMap<Long, EntryPoint> getEntryPoints() {
        return entryPoints;
    }

    public int getMinCardinality() {
        return minCardinality;
    }
}
