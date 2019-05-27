package de.uniks.vs.jalica.behaviours;

import de.uniks.vs.jalica.engine.UtilityFunction;
import de.uniks.vs.jalica.engine.BasicUtilityFunction;
import de.uniks.vs.jalica.engine.common.CommonUtils;
import de.uniks.vs.jalica.engine.DefaultUtilityFunction;
import de.uniks.vs.jalica.engine.model.Plan;

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
