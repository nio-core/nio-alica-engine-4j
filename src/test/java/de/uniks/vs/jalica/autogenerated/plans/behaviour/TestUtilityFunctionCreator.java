package de.uniks.vs.jalica.autogenerated.plans.behaviour;

import de.uniks.vs.jalica.autogenerated.plans.behaviour.utilityfunctions.UtilityFunction1413200842973;
import de.uniks.vs.jalica.autogenerated.plans.behaviour.utilityfunctions.UtilityFunction1413200862180;
import de.uniks.vs.jalica.behaviours.DummyUtilityFunction;
import de.uniks.vs.jalica.behaviours.UtilityFunctionCreator;
import de.uniks.vs.jalica.unknown.BasicUtilityFunction;
import de.uniks.vs.jalica.unknown.CommonUtils;

public class TestUtilityFunctionCreator extends UtilityFunctionCreator {

    public TestUtilityFunctionCreator() { }

    public BasicUtilityFunction createUtility(long utilityfunctionConfId) {

        if (utilityfunctionConfId == 1413200842973l) {
            return new UtilityFunction1413200842973();
        }
        else if (utilityfunctionConfId == 1413200862180l) {
            return new UtilityFunction1413200862180();
        }
        else {
            CommonUtils.aboutError("UtilityFunctionCreator: Unknown utility requested: " + utilityfunctionConfId);
            return new DummyUtilityFunction();
        }

    }

}