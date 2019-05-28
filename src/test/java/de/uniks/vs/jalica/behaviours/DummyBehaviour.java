package de.uniks.vs.jalica.behaviours;

import de.uniks.vs.jalica.engine.AlicaEngine;
import de.uniks.vs.jalica.common.utils.CommonUtils;

/**
 * Created by alex on 01.08.17.
 */
public class DummyBehaviour extends DomainBehaviour {

    public DummyBehaviour(AlicaEngine ae) {
        super("DummyBehaviour", ae);
        /*PROTECTED REGION ID(con1482486255763) ENABLED START*/ //Add additional options here
        CommonUtils.aboutCallNotification();
        /*PROTECTED REGION END*/
    }

    public DummyBehaviour(String name, AlicaEngine ae) {
        super(name, ae);
        CommonUtils.aboutCallNotification();
    }

    public void run(String msg) {
        CommonUtils.aboutNoImpl();
    }

    protected void initialiseParameters() {
        CommonUtils.aboutNoImpl();
    }
}
