package de.uniks.vs.jalica.behaviours.helloworld;

import de.uniks.vs.jalica.behaviours.DomainBehaviour;
import de.uniks.vs.jalica.engine.AlicaEngine;
import de.uniks.vs.jalica.engine.common.CommonUtils;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Publish extends DomainBehaviour {

    /*PROTECTED REGION ID(staticVars1528125245680) ENABLED START*/ //initialise static variables here
    private int countMax;
    private int count;
    /*PROTECTED REGION END*/

    public Publish(AlicaEngine ae) {
        super("Publish", ae);
        /*PROTECTED REGION ID(con1528125245680) ENABLED START*/ //Add additional options here
        countMax = 5;
        count = 0;
        /*PROTECTED REGION END*/
    }

    public void run(String msg) {
        /*PROTECTED REGION ID(run1528125245680) ENABLED START*/ //Add additional options here
        CommonUtils.aboutImplIncomplete();
        ReentrantLock lock = new ReentrantLock();
        Condition stackFullCondition = lock.newCondition();

        try {
            lock.lock();
            stackFullCondition.awaitNanos(countMax);
            count++;
            System.out.println("Publish: " + systemConfig.getOwnAgentID() + "  " + count);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }

        if (count >= countMax)
            this.setSuccess(true);

        /*PROTECTED REGION END*/
    }

    protected void initialiseParameters() {
        /*PROTECTED REGION ID(initialiseParameters1528125245680) ENABLED START*/ //Add additional options here
        CommonUtils.aboutNoImpl();

        countMax = 5;
        count = 0;
        /*PROTECTED REGION END*/
    }
}

