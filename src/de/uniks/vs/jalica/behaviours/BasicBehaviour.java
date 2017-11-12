package de.uniks.vs.jalica.behaviours;

import de.uniks.vs.jalica.supplementary.ITrigger;
import de.uniks.vs.jalica.supplementary.Timer;
import de.uniks.vs.jalica.unknown.CommonUtils;
import de.uniks.vs.jalica.unknown.RunningPlan;
import de.uniks.vs.jalica.unknown.condition_variable;

import java.sql.Time;

/**
 * Created by alex on 14.07.17.
 */
public class BasicBehaviour implements IBehaviourCreator, Runnable {

    private condition_variable runCV;
    private Thread runThread;
    private boolean started;
    private Object parameters;
    private Object variables;
    private Object delayedStart;
    private int interval;
    private RunningPlan runningPlan;
    private boolean success;
    private boolean failure;
    private boolean callInit;
    private ITrigger behaviourTrigger;
    private Timer timer;
    private boolean running;
    private String name;

    public BasicBehaviour(String name) {
        this.name = name;
        this.failure = false;
        this.success = false;
        this.callInit =true; 
        this.started = true;
        this.running = false;
        this.timer = new Timer(0, 0);
        this.timer.registerCV(this.runCV);
        this.runThread = new Thread((Runnable) this);
    }

    public void setParameters(Object parameters) {
        this.parameters = parameters;
    }

    public void setVariables(Object variables) {
        this.variables = variables;
    }

    public void setDelayedStart(Object delayedStart) {
        this.delayedStart = delayedStart;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public void setRunningPlan(RunningPlan runningPlan) {
        this.runningPlan = runningPlan;
    }

    public boolean start() {
        this.callInit = true;
        if (behaviourTrigger == null)
        {
            return this.timer.start();
        }
        else
        {
            this.running = true;
        }
        return true;
    }

    public boolean stop() {
        this.success = false;
        this.failure = false;
        if (behaviourTrigger == null)
        {
            return this.timer.stop();
        }
        else
        {
            this.running = false;
        }
        return true;
    }

    public boolean isSuccess() {
        return success && !this.callInit;
    }

    void setSuccess(boolean success)
    {
        if (!this.success && success)
        {
            this.runningPlan.getAlicaEngine().getPlanBase().addFastPathEvent(this.runningPlan);
        }
        this.success = success;
    }


    void setFailure(boolean failure)
    {
        if (!this.failure && failure)
        {
            this.runningPlan.getAlicaEngine().getPlanBase().addFastPathEvent(this.runningPlan);
        }
        this.failure = failure;
    }



    @Override
    public BasicBehaviour createBehaviour(Long key) {
        CommonUtils.aboutNoImpl();
        return null;
    }

    void runInternal() {
//        unique_lock<mutex> lck(runCV_mtx);
        while (this.started) {
            // TODO: Fix wait
//            this.runCV.wait(
//            {
//                if(behaviourTrigger == null)
//                {
//                    return !this.started || this.timer.isNotifyCalled(runCV);
//                }
//                else
//                {
//                    return !this.started || (this.behaviourTrigger.isNotifyCalled(&runCV) && this.running);
//                }
//            }); // protection against spurious wake-ups

            if (!this.started)
                return;

            if (this.callInit)
            this.initInternal();
//#ifdef BEH_DEBUG
            long start = Timer.getCurrentTimeInNanoSec();
//#endif
            // TODO: pass something like an eventarg (to be implemented) class-member, which could be set for an event triggered (to be implemented) behaviour.
            try
            {
                if (behaviourTrigger == null)
                {
                    CommonUtils.aboutNoImpl();
//                    this.run(timer);
                }
                else
                {
                    CommonUtils.aboutNoImpl();
//                    this.run(behaviourTrigger);
                }
            }
            catch (Exception e)
            {
                String err = "Exception catched:  " + this.getName() + " - " + e.getMessage();
                CommonUtils.aboutError(err);
//                sendLogMessage(4, err);
            }
//#ifdef BEH_DEBUG
            BehaviourConfiguration conf = (BehaviourConfiguration)(this.runningPlan.getPlan());

            if (conf.isEventDriven()) {
                double dura = (Timer.getCurrentTimeInNanoSec() - start) / 1000000.0 - 1.0 / conf.getFrequency() * 1000.0;

                if (dura > 0.1) {
                    String err = "BB: Behaviour " + conf.getBehaviour().getName()+" exceeded runtime by \t" + dura + "ms!";
                    CommonUtils.aboutError(err);
//                    sendLogMessage(2, err);
                    //cout << "BB: Behaviour " << conf.getBehaviour().getName() << " exceeded runtime by \t" << dura
                    //	<< "ms!" << endl;
                }
            }
//#endif
            if (behaviourTrigger == null) {
                this.timer.setNotifyCalled(false, runCV);
            }
            else {
                this.behaviourTrigger.setNotifyCalled(false, runCV);
            }
        }
    }

    private void initInternal() {
        this.success = false;
        this.failure = false;
        this.callInit = false;
        try {
            this.initialiseParameters();
        }
        catch (Exception e) {
            System.err.println("BB: Exception in Behaviour-INIT of: " + this.getName() +"\n" + e.getMessage());
            CommonUtils.aboutError("BB: Exception in Behaviour-INIT of: " + this.getName());
        }
    }

    private void initialiseParameters() {
        CommonUtils.aboutNoImpl();
    }


    public boolean isFailure() {
        return failure && !this.callInit;
    }

    @Override
    public void run() {
        runInternal();
    }

    public String getName() {return name;}
}
