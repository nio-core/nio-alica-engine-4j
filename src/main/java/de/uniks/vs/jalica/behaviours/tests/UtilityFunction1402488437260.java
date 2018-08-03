package de.uniks.vs.jalica.behaviours.tests;

import de.uniks.vs.jalica.common.UtilityFunction;
import de.uniks.vs.jalica.unknown.BasicUtilityFunction;
import de.uniks.vs.jalica.unknown.DefaultUtilityFunction;
import de.uniks.vs.jalica.unknown.Plan;

public class UtilityFunction1402488437260 extends BasicUtilityFunction {

    @Override
    public UtilityFunction getUtilityFunction(Plan plan) {
         UtilityFunction defaultFunction = new DefaultUtilityFunction(plan);
        return defaultFunction;
    }
}
