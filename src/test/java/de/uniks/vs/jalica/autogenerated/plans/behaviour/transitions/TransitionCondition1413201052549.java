package de.uniks.vs.jalica.autogenerated.plans.behaviour.transitions;

import de.uniks.vs.jalica.tests.TestWorldModel;
import de.uniks.vs.jalica.unknown.BasicCondition;
import de.uniks.vs.jalica.unknown.Condition;
import de.uniks.vs.jalica.unknown.RunningPlan;

public class TransitionCondition1413201052549 extends BasicCondition {

    @Override
    protected boolean evaluate(RunningPlan rp) {
        /*PROTECTED REGION ID(1413201052549) ENABLED START*/
        if(rp.getOwnID() == 8)
        {
            return TestWorldModel.getOne().isTransitionCondition1413201052549();
        }
        else
        {
            return TestWorldModel.getTwo().isTransitionCondition1413201052549();
        }
        /*PROTECTED REGION END*/
    }

}
