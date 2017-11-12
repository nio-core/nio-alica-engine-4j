package de.uniks.vs.jalica.behaviours;

import de.uniks.vs.jalica.unknown.CommonUtils;

/**
 * Created by alex on 01.08.17.
 */
public class DummyBehaviour extends DomainBehaviour {

    public DummyBehaviour() {
        super("DummyBehaviour");
        /*PROTECTED REGION ID(con1482486255763) ENABLED START*/ //Add additional options here
        /*PROTECTED REGION END*/
    }

    public DummyBehaviour(String name) {
        super(name);
    }

    public void run(String msg) {
        CommonUtils.aboutNoImpl();
    }

    protected void initialiseParameters() {
        CommonUtils.aboutNoImpl();
    }
}
