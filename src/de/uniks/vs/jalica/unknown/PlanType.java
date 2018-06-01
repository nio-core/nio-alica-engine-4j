package de.uniks.vs.jalica.unknown;

import de.uniks.vs.jalica.engine.AlicaEngine;

import java.util.ArrayList;

/**
 * Created by alex on 13.07.17.
 */
public class PlanType extends AbstractPlan {
    private ArrayList<Plan> plans;

    public PlanType(AlicaEngine ae) {
        super(ae);
    }

    public ArrayList<Plan> getPlans() {
        return plans;
    }
}
