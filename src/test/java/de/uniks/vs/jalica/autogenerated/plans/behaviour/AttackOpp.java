package de.uniks.vs.jalica.autogenerated.plans.behaviour;

import de.uniks.vs.jalica.behaviours.DomainBehaviour;
import de.uniks.vs.jalica.engine.AlicaEngine;
import de.uniks.vs.jalica.common.utils.CommonUtils;

public class AttackOpp extends DomainBehaviour {

    /*PROTECTED REGION ID(staticVars1402489351885) ENABLED START*/ //initialise static variables here
    /*PROTECTED REGION END*/

    public AttackOpp(AlicaEngine ae) {
        super("AttackOpp", ae);
        /*PROTECTED REGION ID(con1402489351885) ENABLED START*/ //Add additional options here
        /*PROTECTED REGION END*/
    }

    @Override
    public void run(String msg) {
        /*PROTECTED REGION ID(run1402489351885) ENABLED START*/ //Add additional options here
        CommonUtils.aboutCallNotification("AttackOpp " );
        /*PROTECTED REGION END*/
    }

    @Override
    protected void initialiseParameters() {
        /*PROTECTED REGION ID(initialiseParameters1402489351885) ENABLED START*/ //Add additional options here
        /*PROTECTED REGION END*/
    }

    /*PROTECTED REGION ID(methods1402489351885) ENABLED START*/ //Add additional methods here
    /*PROTECTED REGION END*/
}
