package de.uniks.vs.jalica.behaviours;

import de.uniks.vs.jalica.common.UtilityFunction;
import de.uniks.vs.jalica.unknown.BasicUtilityFunction;
import de.uniks.vs.jalica.unknown.CommonUtils;
import de.uniks.vs.jalica.unknown.DefaultUtilityFunction;
import de.uniks.vs.jalica.unknown.Plan;

public class DummyUtilityFunction extends BasicUtilityFunction {

    public DummyUtilityFunction() {
        CommonUtils.aboutCallNotification();
    }

    @Override
    public UtilityFunction getUtilityFunction(Plan plan) {
        UtilityFunction defaultFunction = new DefaultUtilityFunction(plan);
        System.out.println("UF: Dummy " + plan.getName());
        return defaultFunction;
    }
}
