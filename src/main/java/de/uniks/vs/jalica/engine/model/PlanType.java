package de.uniks.vs.jalica.engine.model;

import de.uniks.vs.jalica.engine.AlicaEngine;
import de.uniks.vs.jalica.engine.common.Parametrisation;

import java.util.ArrayList;

/**
 * Created by alex on 13.07.17.
 */
public class PlanType extends AbstractPlan {
    private ArrayList<Plan> plans = new ArrayList<>();
    private ArrayList<Parametrisation> parametrisation = new ArrayList<>();

    public PlanType(AlicaEngine ae) {
        super(ae);
    }

    public ArrayList<Plan> getPlans() {
        return plans;
    }

    public ArrayList<Parametrisation> getParametrisation() {
        return parametrisation;
    }
}
