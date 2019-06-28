package de.uniks.vs.jalica.autogenerated.plans.behaviour.transitions;

import de.uniks.vs.jalica.newtests.TestWorldModel;
import de.uniks.vs.jalica.engine.BasicCondition;
import de.uniks.vs.jalica.engine.RunningPlan;

public class TransitionCondition1413201370590 extends BasicCondition {

    @Override
    public boolean evaluate(RunningPlan rp) {
        /*PROTECTED REGION ID(1413201370590) ENABLED START*/
        if(rp.getAlicaEngine().getTeamManager().getLocalAgentID() == 8)
        {
            return TestWorldModel.getOne().isTransitionCondition1413201370590();
        }
        else
        {
            return TestWorldModel.getTwo().isTransitionCondition1413201370590();
        }
        /*PROTECTED REGION END*/
    }

}
