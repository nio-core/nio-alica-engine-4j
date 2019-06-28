package de.uniks.vs.jalica.autogenerated.plans.behaviour.transitions;

import de.uniks.vs.jalica.newtests.TestWorldModel;
import de.uniks.vs.jalica.engine.BasicCondition;
import de.uniks.vs.jalica.engine.RunningPlan;

public class TransitionCondition1413201367990 extends BasicCondition {

    @Override
    public boolean evaluate(RunningPlan rp) {
        /*PROTECTED REGION ID(1413201226246) ENABLED START*/
        if(rp.getAlicaEngine().getTeamManager().getLocalAgentID() == 8)
        {
            return TestWorldModel.getOne().isTransitionCondition1413201367990();
        }
        else
        {
            return TestWorldModel.getTwo().isTransitionCondition1413201367990();
        }
        /*PROTECTED REGION END*/
    }

}
