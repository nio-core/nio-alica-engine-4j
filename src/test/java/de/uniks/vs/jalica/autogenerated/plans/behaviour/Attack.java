package de.uniks.vs.jalica.autogenerated.plans.behaviour;

import de.uniks.vs.jalica.behaviours.DomainBehaviour;
import de.uniks.vs.jalica.engine.AlicaEngine;
import de.uniks.vs.jalica.common.utils.CommonUtils;

/*PROTECTED REGION ID(inccpp1402488848841) ENABLED START*/ //Add additional includes here
/*PROTECTED REGION END*/

public class Attack extends DomainBehaviour {
    public int callCounter;
    private int initCounter;

    /*PROTECTED REGION ID(staticVars1402488848841) ENABLED START*/ //initialise static variables here
    /*PROTECTED REGION END*/
    public Attack(AlicaEngine ae) {
        super("Attack", ae);
        /*PROTECTED REGION ID(con1402488848841) ENABLED START*/ //Add additional options here
        this.callCounter = 0;
        this.initCounter = 0;
        /*PROTECTED REGION END*/
    }

    @Override
    public void run(String msg) {
        /*PROTECTED REGION ID(run1402488848841) ENABLED START*/ //Add additional options here
        CommonUtils.aboutCallNotification("Attack " + callCounter );
        callCounter++;
        /*PROTECTED REGION END*/
    }

    @Override
    protected void initialiseParameters() {
        /*PROTECTED REGION ID(initialiseParameters1402488848841) ENABLED START*/ //Add additional options here
        callCounter = 0;
        initCounter++;
        /*PROTECTED REGION END*/
    }

    /*PROTECTED REGION ID(methods1402488848841) ENABLED START*/ //Add additional methods here
    /*PROTECTED REGION END*/
}
