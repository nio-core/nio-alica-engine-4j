package de.uniks.vs.jalica.behaviours;

import de.uniks.vs.jalica.supplementary.Trigger;
import de.uniks.vs.jalica.supplementary.TimerEvent;
import de.uniks.vs.jalica.unknown.CommonUtils;
import de.uniks.vs.jalica.unknown.RunningPlan;
import de.uniks.vs.jalica.unknown.Variable;
import de.uniks.vs.jalica.unknown.ConditionVariable;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by alex on 14.07.17.
 */
public abstract class BasicBehaviour implements /*IBehaviourCreator*/ Runnable {

    private ConditionVariable runCV;
    private Thread runThread;
    private HashMap<String, String> parameters;
    private ArrayList<Variable> variables;
    private RunningPlan runningPlan;
    private boolean started;
    private boolean success;
    private boolean failure;
    private boolean callInit;
    private boolean running;
    private Trigger behaviourTrigger;
    private TimerEvent timer;
    private int delayedStart;
    private int interval;
    private String name;

    public BasicBehaviour(String name) {
        if (CommonUtils.B_DEBUG_debug) System.out.println("BB: constructor called " + name);
        this.name = name;
        this.failure =  false;
        this.success =  false;
        this.callInit = true;
        this.started =  true;
        this.running =  false;
        this.timer = new TimerEvent(3300, 0, this.getClass().getSimpleName());
//        this.runThread = new Thread(this);
//        this.runCV = new ConditionVariable(this.runThread);
        this.runCV = new ConditionVariable(this);
        this.timer.addConditionVariable(this.runCV);
        //TODO: change to start on demand
//        this.runThread.start();
        this.runThread = new Thread(this);
        this.runThread.start();
    }

    public boolean start() {

//        if (!this.runThread.isAlive())
//            this.runThread.start();
        this.callInit = true;

        if (behaviourTrigger == null) {
            if (CommonUtils.B_DEBUG_debug) System.out.println("BB: start behaviour type -> " + this.getClass().getSimpleName());
            CommonUtils.aboutCallNotification();
            return this.timer.start();
        }
        else {
            CommonUtils.aboutCallNotification();
            this.running = true;
        }
        CommonUtils.aboutCallNotification();
        return true;
    }

    public boolean stop() {
        this.success = false;
        this.failure = false;

        if (behaviourTrigger == null) {

            if (CommonUtils.B_DEBUG_debug) System.out.println("BB: stop behaviour type -> " + this.getClass().getSimpleName());
            CommonUtils.aboutImplIncomplete();
            return this.timer.stop();
        }
        else {
            this.running = false;
        }
        return true;
    }

    public boolean isSuccess() {
        return success && !this.callInit;
    }

    protected void setSuccess(boolean success)
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



//    @Override
//    public BasicBehaviour createBehaviour(Long key,  AlicaEngine ae) {
//        CommonUtils.aboutNoImpl();
//        return null;
//    }

    void runInternal() {
//        unique_lock<mutex> lck(runCV_mtx);
        CommonUtils.aboutCallNotification();
        while (this.started) {

            if (CommonUtils.B_DEBUG_debug) CommonUtils.aboutCallNotification(this.getClass().getSimpleName());
            // TODO: Fix wait
            try {
                //            this.runCV.wait(
//            {
//                if(behaviourTrigger == null) {
//                    return !this.started || this.timer.isNotifyCalled(runCV);
//                }
//                else {
//                    return !this.started || (this.behaviourTrigger.isNotifyCalled(runCV) && this.running);
//                }
//            }
//            ); // protection against spurious wake-ups
                Thread conditionVariable = new Thread() {

                    @Override
                    public void run() {
                        CommonUtils.aboutCallNotification("thread started");

                        while (true) {
                            if(behaviourTrigger == null && (!started || timer.isNotifyCalled(runCV))) {
                                if (CommonUtils.B_DEBUG_debug) CommonUtils.aboutCallNotification("notify");
                                synchronized (runThread) {
                                    runThread.notify();
                                }
                                return;
                            }
                            else if (!started || (behaviourTrigger != null && behaviourTrigger.isNotifyCalled(runCV) && running)) {
                                if (CommonUtils.B_DEBUG_debug) CommonUtils.aboutCallNotification( "notify");
                                synchronized (runThread) {
                                    runThread.notify();
                                }
                                return;
                            }

                            try {
                                Thread.sleep(10);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                };
                conditionVariable.start();

                synchronized (this) {
                    this.wait();
                }

                if (CommonUtils.B_DEBUG_debug) System.out.println("BB: " + this.getClass().getSimpleName() + "  " + name + " awakened");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (!this.started)
                return;

            if (this.callInit)
                this.initInternal();
//#ifdef BEH_DEBUG
            long start = TimerEvent.getCurrentTimeInNanoSec();
//#endif
            // TODO: pass something like an eventarg (to be implemented) class-member, which could be set for an event triggered (to be implemented) behaviour.
            try {
                if (CommonUtils.B_DEBUG_debug) CommonUtils.aboutCallNotification();

                if (behaviourTrigger == null) {
                    if (CommonUtils.B_DEBUG_debug)  CommonUtils.aboutCallNotification();
                    this.run(timer.getClass().getSimpleName());
                }
                else {
                    if (CommonUtils.B_DEBUG_debug) CommonUtils.aboutCallNotification();
                    this.run(behaviourTrigger.getClass().getSimpleName());
                }
            }
            catch (Exception e) {
                CommonUtils.aboutError("Exception catched:  " + this.getName() + " - " + e.getMessage());
                System.out.flush();
                e.printStackTrace();
                System.err.flush();
//                sendLogMessage(4, err);
            }
//#ifdef BEH_DEBUG
            BehaviourConfiguration conf = (BehaviourConfiguration)(this.runningPlan.getPlan());

            if (conf.isEventDriven()) {
                double dura = (TimerEvent.getCurrentTimeInNanoSec() - start) / 1000000.0 - 1.0 / conf.getFrequency() * 1000.0;

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
                if (CommonUtils.B_DEBUG_debug) CommonUtils.aboutCallNotification("setNotifyCalled false");
                this.timer.setNotifyCalled(false, runCV);
            }
            else {
                if (CommonUtils.B_DEBUG_debug) CommonUtils.aboutCallNotification("setNotifyCalled false");
                this.behaviourTrigger.setNotifyCalled(false, runCV);
            }
        }
    }

    private void initInternal() {
        CommonUtils.aboutCallNotification();
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

    protected void initialiseParameters() {
        CommonUtils.aboutNoImpl();
    }

    public boolean isFailure() {
        return failure && !this.callInit;
    }

    @Override
    public void run() {
        runInternal();
    }

    public abstract void run(String msg);

    public String getName() {return name;}

    public void setParameters(HashMap<String, String> parameters) {
        this.parameters = parameters;
    }

    public void setVariables(ArrayList<Variable> variables) {
        this.variables = variables;
    }

    public void setDelayedStart(int delayedStart) {
        this.timer.setDelayedStart(delayedStart);
    }

    public long getDelayedStart() {
        return this.timer.getDelayedStart();
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public void setRunningPlan(RunningPlan runningPlan) {
        if (CommonUtils.B_DEBUG_debug)  CommonUtils.aboutCallNotification();
        this.runningPlan = runningPlan;
    }

    public void setTrigger(Trigger behaviourTrigger) {
        CommonUtils.aboutCallNotification();
        this.behaviourTrigger = behaviourTrigger;
        this.behaviourTrigger.addConditionVariable(this.runCV);
    }
}
