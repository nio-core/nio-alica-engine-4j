package de.uniks.vs.jalica.behaviours.helloworld.testcommunicationmaster1528124971225;

import de.uniks.vs.jalica.behaviours.DomainCondition;
import de.uniks.vs.jalica.unknown.CommonUtils;
import de.uniks.vs.jalica.unknown.RunningPlan;
import de.uniks.vs.jalica.unknown.State;

public class TransitionCondition1528125190720 extends DomainCondition {
    //State: TestStop in Plan: TestCommunicationMaster

    /*
     *
     * Transition:
     *   - Name: MISSING_NAME, ConditionString: , Comment : Start signal received
     *
     * Plans in State:
     *
     * Tasks:
     *   - DefaultTask (1414681164704) (Entrypoint: 1528124971227)
     *
     * States:
     *   - TestStop (1528124971226)
     *   - TestSart (1528125127758)
     *
     * Vars:
     */
    public boolean evaluate(RunningPlan rp) {
        /*PROTECTED REGION ID(1528125189377) ENABLED START*/
        CommonUtils.aboutCallNotification();
        System.out.println("TC: " + rp.getActiveState().getName());
        return true;
        /*PROTECTED REGION END*/

    }

//State: TestSart in Plan: TestCommunicationMaster
}
