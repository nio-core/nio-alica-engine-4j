package de.uniks.vs.jalica.unknown;

import java.util.ArrayList;

/**
 * Created by alex on 14.07.17.
 */
public class SimplePlanTree {
    private ArrayList<SimplePlanTree> children;
    private RunningPlan entryPoint;

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

    public ArrayList<SimplePlanTree> getChildren() {
        return children;
    }

    public RunningPlan getEntryPoint() {
        return entryPoint;
    }
}
