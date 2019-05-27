package de.uniks.vs.jalica.behaviours.tests;

import de.uniks.vs.jalica.engine.UtilityFunction;
import de.uniks.vs.jalica.engine.BasicUtilityFunction;
import de.uniks.vs.jalica.engine.DefaultUtilityFunction;
import de.uniks.vs.jalica.engine.model.Plan;

public class UtilityFunction1402488437260 extends BasicUtilityFunction {

    @Override
    public UtilityFunction getUtilityFunction(Plan plan) {
         UtilityFunction defaultFunction = new DefaultUtilityFunction(plan);
        return defaultFunction;
    }
}
