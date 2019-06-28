package de.uniks.vs.jalica.behaviours.constraints;

import de.uniks.vs.jalica.engine.constrainmodule.ProblemDescriptor;
import de.uniks.vs.jalica.engine.BasicConstraint;
import de.uniks.vs.jalica.common.utils.CommonUtils;
import de.uniks.vs.jalica.engine.RunningPlan;

public class Constraint1403773741874 extends BasicConstraint {

    //Plan:GoalPlan

    /*
     * Tasks:
     * - EP:1402488881800 : DefaultTask (1225112227903)
     *
     * States:
     * - Shoot (1402488881799)
     * - Miss (1402489152217)
     * - Scored (1402489192198)
     *
     * Vars:
     * - test (1403773747758)
     */

    /*
     * RuntimeCondition - (Name): NewRuntimeCondition
     * (ConditionString): test
     * Static Variables: [test]
     * Domain Variables:

     * forall agents in Miss let v = [test]

     */
    @Override
    public void getConstraint(ProblemDescriptor c, RunningPlan rp) {
        /*PROTECTED REGION ID(cc1403773741874) ENABLED START*/
        CommonUtils.aboutCallNotification();
        System.out.println("RTC: " + rp.getActivePlan().getName());
        /*PROTECTED REGION END*/
    }

// State: Shoot

// State: Shoot

// State: Miss

// State: Miss

// State: Scored

// State: Scored
}
