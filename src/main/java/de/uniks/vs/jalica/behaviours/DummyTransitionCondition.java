package de.uniks.vs.jalica.behaviours;

import de.uniks.vs.jalica.unknown.BasicCondition;
import de.uniks.vs.jalica.unknown.CommonUtils;
import de.uniks.vs.jalica.unknown.RunningPlan;

public class DummyTransitionCondition extends BasicCondition {

    public DummyTransitionCondition() {
        CommonUtils.aboutCallNotification();
    }

    protected boolean evaluate(RunningPlan rp) {
        /*PROTECTED REGION ID(1528125075116) ENABLED START*/
        System.out.println("TC: Dummy " + rp.getActiveState().getName());
        CommonUtils.aboutNoImpl();
        return true;
        /*PROTECTED REGION END*/
    }

//State: Success in Plan: TestCommunication
}
