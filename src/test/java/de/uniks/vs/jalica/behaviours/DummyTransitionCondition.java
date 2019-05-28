package de.uniks.vs.jalica.behaviours;

import de.uniks.vs.jalica.engine.BasicCondition;
import de.uniks.vs.jalica.common.utils.CommonUtils;
import de.uniks.vs.jalica.engine.RunningPlan;

public class DummyTransitionCondition extends BasicCondition {

    public DummyTransitionCondition() {
        CommonUtils.aboutCallNotification();
    }

    @Override
    public boolean evaluate(RunningPlan rp) {
        /*PROTECTED REGION ID(1528125075116) ENABLED START*/
        System.out.println("TC: Dummy condition " + rp.getActiveState().getName());
        CommonUtils.aboutNoImpl();
        return false;
        /*PROTECTED REGION END*/
    }

//State: Success in Plan: TestCommunication
}
