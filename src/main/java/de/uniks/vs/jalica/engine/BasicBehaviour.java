package de.uniks.vs.jalica.engine;

import de.uniks.vs.jalica.common.CVCondition;
import de.uniks.vs.jalica.common.Trigger;
import de.uniks.vs.jalica.common.TimerEvent;
import de.uniks.vs.jalica.common.utils.CommonUtils;
import de.uniks.vs.jalica.engine.idmanagement.ID;
import de.uniks.vs.jalica.engine.model.Behaviour;
import de.uniks.vs.jalica.engine.model.Variable;
import de.uniks.vs.jalica.common.ConditionVariable;
import de.uniks.vs.jalica.engine.model.BehaviourConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by alex on 14.07.17.
 * update 21.6.19
 */
public abstract class BasicBehaviour implements Runnable {

    private String name;
    private Behaviour behaviour;
    private BehaviourConfiguration behaviourConfiguration;
    private AlicaEngine engine;
    private RunningPlan context;

    private boolean started;
    private boolean callInit;

    private boolean success;
    private boolean failure;
    private boolean running;
    private RunningPlan contextInRun;

    private Thread runThread;

    private ITrigger behaviourTrigger;
    private ConditionVariable runCV;

    private Lock runLoopMutex;
    private long msInterval ;
    private long msDelayedStart;

//    private ConditionVariable runCV;
//    private Thread runThread;
//    private HashMap<String, String> parameters;
//    private ArrayList<Variable> variables;
//    private RunningPlan runningPlan;
//    private boolean started;
//    private boolean finished;
//    private boolean success;
//    private boolean failure;
//    private boolean callInit;
//    private boolean running;
//    private boolean loop;
//    private Trigger behaviourTrigger;
//    private TimerEvent timer;
//    private int delayedStart;
//    private int interval;
//    private String name;

    public BasicBehaviour(String name) {
        if (CommonUtils.B_DEBUG_debug) System.out.println("BB: constructor called " + name);
        this.name = name;
        this.behaviourConfiguration = null;
        this.engine = null;
        this.failure = false;
        this.success = false;
        this.callInit = true;
        this.started = true;
        this.behaviour = null;
        this.msInterval = 100;
        this.msDelayedStart = 0;
        this.running = false;
        this.contextInRun = null;
        this.behaviourTrigger = null;
        this.runThread = null;
        this.context = null;
        this.runCV = new ConditionVariable(this);
        this.runLoopMutex = new ReentrantLock();

//        this.name = name;
//        this.loop =  false;
//        this.finished =  false;
//        this.failure =  false;
//        this.success =  false;
//        this.callInit = true;
//        this.started =  true;
//        this.running =  false;
//        this.timer = new TimerEvent(100, 0, this.getClass().getSimpleName());
////        this.runCV = new ConditionVariable(this.runThread);
//        this.runCV = new ConditionVariable(this);
//        this.timer.addConditionVariable(this.runCV);
//        //TODO: change teamObserver start on demand
//        this.runThread = new Thread(this);
//        this.runThread.start();
    }

    public abstract void run(Object msg);

    public void init() {}

    void terminate() {
        this.started = false;
        this.runCV.notifyAll();

        if (this.runThread != null) {
            try {
                this.runThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isRunningInContext( RunningPlan rp) {
        // we run in the context of rp if rp is the context in run or the context in run is null and context is rp
        RunningPlan curInRun;
        curInRun = this.contextInRun;
        return curInRun == rp || (curInRun == null && this.context == rp && this.started && this.running);
    }

    public void setBehaviourConfiguration( BehaviourConfiguration behConf) {
        assert(this.behaviourConfiguration == null);
        this.behaviourConfiguration = behConf;
    }

    public void setBehaviour( Behaviour beh) {
        assert(this.behaviour == null);
        this.behaviour = beh;
        beh.setImplementation(this);

        if (this.behaviour.isEventDriven()) {
//            this.runThread = new Thread(BasicBehaviour::runInternalTriggered, this);
            this.runThread = new Thread(this) {
                @Override
                public void run() {
                    runInternalTriggered();
                }
            };
        } else {
//            this.runThread = new Thread(BasicBehaviour::runInternalTimed, this);
            this.runThread = new Thread(this) {
                @Override
                public void run() {
                    runInternalTimed();
                }
            };
        }
        this.runThread.start();
    }

    public ID getOwnId() {
        return this.engine.getTeamManager().getLocalAgentID();
    }

    public boolean stop() {
        this.running = false;
        this.success = false;
        this.failure = false;
        return true;
    }

    public boolean start() {
        this.callInit = true;
        this.running = true;

        if (!this.behaviour.isEventDriven()) {
            this.runCV.notifyAllThreads();
        }
        return true;
    }

    public void setSuccess() {
        if (!this.success) {
            this.success = true;
            this.engine.getPlanBase().addFastPathEvent(this.context);
        }
    }

    public  boolean isSuccess() {
        return this.success && !this.callInit;
    }

    public void setFailure() {
        if (!this.failure) {
            this.failure = true;
            this.engine.getPlanBase().addFastPathEvent(this.context);
        }
    }

    public boolean isFailure() {
        return this.failure && !this.callInit;
    }

    public void setTrigger(ITrigger trigger) {
        this.behaviourTrigger = trigger;
        this.behaviourTrigger.registerCV(this.runCV);
    }

    protected void initInternal() {
        this.success = false;
        this.failure = false;
        this.callInit = false;
        try {
            initialiseParameters();
        } catch ( Exception e) {
        CommonUtils.aboutError("BB: Exception in Behaviour-INIT of: " + getName() + "\n" + e.getMessage());
    }
    }

    protected void runInternalTimed() {

        while (this.started) {
            {
//                std::unique_lock<std::mutex> lck(_runLoopMutex);
                Lock lck = this.runLoopMutex;
                lck.lock();
                if (!this.running) {

                    if (this.contextInRun != null) {
                        onTermination();
                    }
                    this.contextInRun = null;
//                    this.runCV.wait(lck, [this] { return this.running || !this.started; }); // wait for signal to run
                    this.runCV.cvWait(lck, this, () -> running || !started); // wait for signal to run
                }
                this.contextInRun = this.context;
                lck.unlock();
            }
            if (!this.started) {
                this.contextInRun = null;
                return;
            }
            if (this.callInit) {
                if (this.msDelayedStart > 0) {
                    try {
                        Thread.sleep(this.msDelayedStart);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                initInternal();
            }
            long start = System.nanoTime();//std::chrono::high_resolution_clock::now();
            try {
                run(null);
            } catch ( Exception e) {
                String err =  "Exception caught:  " + getName() + " - " + e.getMessage();
                sendLogMessage(4, err);
            }
            long duration = System.nanoTime() - start;
            CommonUtils.aboutWarningNotification(duration > this.msInterval + 100000 /*100 microseconds*/,
                    "BB: Behaviour " + this.name + " exceeded runtime:  " + duration + "ms!");
            if (duration < this.msInterval) {
                try {
                    Thread.sleep(this.msInterval - duration);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    protected void runInternalTriggered() {

        while (this.started) {
            {
                if (this.contextInRun != null) {
                    onTermination();
                }
//                std::unique_lock<std::mutex> lck(this.runLoopMutex);
                Lock lck = this.runLoopMutex;
                lck.lock();
                this.contextInRun = null;
//                this.runCV.wait(lck, [this] { return !this.started || (this.behaviourTrigger.isNotifyCalled(this.runCV) && this.running); });
                this.runCV.cvWait(lck, this, () -> !this.started || (this.behaviourTrigger.isNotifyCalled(this.runCV) && this.running));
                this.contextInRun = this.started ? this.context : null;
                lck.unlock();
            }
            if (!this.started) {
                return;
            }
            if (this.callInit) {
                initInternal();
            }

            try {
                run(this.behaviourTrigger);
            } catch ( Exception e) {
                String err = "Exception caught:  " + getName() + " - " + e.getMessage();
                sendLogMessage(4, err);
            }
            this.behaviourTrigger.setNotifyCalled(false, this.runCV);
        }
    }

    public void sendLogMessage(int level, String message) {
        this.engine.getCommunicator().sendLogMessage(level, message);
    }

    // if (key exists -> true else false)
    public String getParameter(String key, String valueOut) {
        String value = this.behaviour.getParameters().get(key);

        if (value != null) {
            return value;
        } else {
            return valueOut;
        }
    }

    // --- getter setter ---

    public void setEngine(AlicaEngine engine) { this.engine = engine; }
    public String getName() { return this.name; }
    public ArrayList<Variable> getVariables() { return this.behaviour.getVariables(); }
    public Variable getVariable(String name) { return this.behaviour.getVariable(name); };
    public void setDelayedStart(long msDelayedStart) { this.msDelayedStart = msDelayedStart; }
    public void setInterval(long msInterval) { this.msInterval = msInterval; }
    public ThreadSafePlanInterface getPlanContext() { return new ThreadSafePlanInterface(this.contextInRun); }
    public void setRunningPlan(RunningPlan rp) { this.context = rp; }

    protected AlicaEngine getEngine()  { return this.engine; }
    protected void initialiseParameters() {}
    protected void onTermination() {}


    // ---- NEW -----------------
    boolean loop;
    boolean finished;

    public boolean isFinished() {
        return this.finished;
    }

    public void isLoop(boolean loop) {
        this.loop = loop;
    }

    public void notifyLoopFinished() {
        this.finished = !this.loop;
    }


//    public boolean isRunningInContext( RunningPlan rp) {
//        // we run in the context of rp if rp is the context in run or the context in run is null and context is rp
//        RunningPlan curInRun = null;
//
//        if (this.running)
//            curInRun = this.runningPlan;
//        return curInRun == rp || (curInRun == null && this.runningPlan == rp && this.started && this.running);
////        curInRun = this.contextInRun;
////        return curInRun == rp || (curInRun == null && this.context == rp && this.started && this.running);
//    }
//
//    public boolean start() {
////        if (!this.runThread.isAlive())
////            this.runThread.start();
//        this.callInit = true;
//
//        if (behaviourTrigger == null) {
//            if (CommonUtils.B_DEBUG_debug) System.out.println("BB: start behaviour type -> " + this.getClass().getSimpleName());
//            if (CommonUtils.B_DEBUG_debug) CommonUtils.aboutCallNotification();
//            //TODO: check
////            synchronized (runCV) {
////                runCV.notifyAll();
////            }
//            return this.timer.start();
//        }
//        else {
//            CommonUtils.aboutCallNotification();
//            this.running = true;
//        }
//        CommonUtils.aboutCallNotification();
//        return true;
//    }
//
//    public boolean stop() {
//        this.success = false;
//        this.failure = false;
//
//        if (behaviourTrigger == null) {
//
//            if (CommonUtils.B_DEBUG_debug) System.out.println("BB: stop behaviour type -> " + this.getClass().getSimpleName());
//            if (CommonUtils.B_DEBUG_debug) CommonUtils.aboutImplIncomplete();
//            return this.timer.stop();
//        }
//        else {
//            this.running = false;
//        }
//        return true;
//    }
//
//    public boolean isSuccess() {
//        return success && !this.callInit;
//    }
//
//    protected void setSuccess(boolean success)
//    {
//        if (!this.success && success)
//        {
//            this.runningPlan.getAlicaEngine().getPlanBase().addFastPathEvent(this.runningPlan);
//        }
//        this.success = success;
//    }
//
//
//    void setFailure(boolean failure)
//    {
//        if (!this.failure && failure)
//        {
//            this.runningPlan.getAlicaEngine().getPlanBase().addFastPathEvent(this.runningPlan);
//        }
//        this.failure = failure;
//    }
//
//
//
////    @Override
////    public BasicBehaviour createBehaviour(Long key,  AlicaEngine ae) {
////        CommonUtils.aboutNoImpl();
////        return null;
////    }
//
//    void runInternal() {
////        unique_lock<mutex> lck(runCV_mtx);
//        if (CommonUtils.B_DEBUG_debug) CommonUtils.aboutCallNotification();
//        while (this.started) {
//
//            if (CommonUtils.B_DEBUG_debug) CommonUtils.aboutCallNotification(this.getClass().getSimpleName());
//            // TODO: Fix wait
//            try {
//                //            this.runCV.wait(
////            {
////                if(behaviourTrigger == null) {
////                    return !this.started || this.timer.isNotifyCalled(runCV);
////                }
////                else {
////                    return !this.started || (this.behaviourTrigger.isNotifyCalled(runCV) && this.running);
////                }
////            }
////            ); // protection against spurious wake-ups
//                Thread conditionVariable = new Thread() {
//
//                    @Override
//                    public void run() {
//                        if (CommonUtils.B_DEBUG_debug)  CommonUtils.aboutCallNotification("thread started");
//
//                        while (true) {
//                            if(behaviourTrigger == null && (!started || timer.isNotifyCalled(runCV))) {
//                                if (CommonUtils.B_DEBUG_debug) CommonUtils.aboutCallNotification("notify");
//                                synchronized (runThread) {
//                                    runThread.notify();
//                                }
//                                return;
//                            }
//                            else if (!started || (behaviourTrigger != null && behaviourTrigger.isNotifyCalled(runCV) && running)) {
//                                if (CommonUtils.B_DEBUG_debug) CommonUtils.aboutCallNotification( "notify");
//                                synchronized (runThread) {
//                                    runThread.notify();
//                                }
//                                return;
//                            }
//
//                            try {
//                                Thread.sleep(30);
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    }
//                };
//                conditionVariable.start();
//
//                synchronized (this) {
//                    this.wait();
//                }
//
//                if (CommonUtils.B_DEBUG_debug) System.out.println("BB: " + this.getClass().getSimpleName() + "  " + name + " awakened");
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//
//            if (!this.started)
//                return;
//
//            if (this.callInit)
//                this.initInternal();
////#ifdef BEH_DEBUG
//            long start = TimerEvent.getCurrentTimeInNanoSec();
////#endif
//            // TODO: pass something like an eventarg (teamObserver be implemented) class-member, which could be set for an event triggered (teamObserver be implemented) behaviour.
//            try {
//                if (CommonUtils.B_DEBUG_debug) CommonUtils.aboutCallNotification();
//
//                if (behaviourTrigger == null) {
//                    if (CommonUtils.B_DEBUG_debug)  CommonUtils.aboutCallNotification();
//                    this.run(timer.getClass().getSimpleName());
//                }
//                else {
//                    if (CommonUtils.B_DEBUG_debug) CommonUtils.aboutCallNotification();
//                    this.run(behaviourTrigger.getClass().getSimpleName());
//                }
//            }
//            catch (Exception e) {
//                CommonUtils.aboutError("Exception catched:  " + this.getName() + " - " + e.getMessage());
//                System.out.flush();
//                e.printStackTrace();
//                System.err.flush();
////                sendLogMessage(4, err);
//            }
////#ifdef BEH_DEBUG
//            BehaviourConfiguration conf = (BehaviourConfiguration)(this.runningPlan.getPlan());
//
//            if (conf.isEventDriven()) {
//                double dura = (TimerEvent.getCurrentTimeInNanoSec() - start) / 1000000.0 - 1.0 / conf.getFrequency() * 1000.0;
//
//                if (dura > 0.1) {
//                    String err = "BB: Behaviour " + conf.getBehaviour().getName()+" exceeded runtime by \t" + dura + "ms!";
//                    CommonUtils.aboutError(err);
////                    sendLogMessage(2, err);
//                    //cout << "BB: Behaviour " << conf.getBehaviour().getName() << " exceeded runtime by \t" << dura
//                    //	<< "ms!" << endl;
//                }
//            }
////#endif
//            if (behaviourTrigger == null) {
//                if (CommonUtils.B_DEBUG_debug) CommonUtils.aboutCallNotification("setNotifyCalled false");
//                this.timer.setNotifyCalled(false, runCV);
//            }
//            else {
//                if (CommonUtils.B_DEBUG_debug) CommonUtils.aboutCallNotification("setNotifyCalled false");
//                this.behaviourTrigger.setNotifyCalled(false, runCV);
//            }
//        }
//    }
//
//    private void initInternal() {
//        if (CommonUtils.B_DEBUG_debug) CommonUtils.aboutCallNotification();
//        this.success = false;
//        this.failure = false;
//        this.callInit = false;
//        try {
//            this.initialiseParameters();
//        }
//        catch (Exception e) {
//            System.err.println("BB: Exception in Behaviour-INIT of: " + this.getName() +"\n" + e.getMessage());
//            CommonUtils.aboutError("BB: Exception in Behaviour-INIT of: " + this.getName());
//        }
//    }
//
//    protected void initialiseParameters() {
//        CommonUtils.aboutNoImpl();
//    }
//
//    public boolean isFailure() {
//        return failure && !this.callInit;
//    }
//
//    @Override
//    public void run() {
//        runInternal();
//        CommonUtils.aboutCallNotification();
//    }
//
//    public abstract void run(String msg);
//
//    public String getName() {return name;}
//
//    public void setParameters(HashMap<String, String> parameters) {
//        this.parameters = parameters;
//    }
//
//    public void setVariables(ArrayList<Variable> variables) {
//        this.variables = variables;
//    }
//
//    public void setDelayedStart(int delayedStart) {
//        this.timer.setDelayedStart(delayedStart);
//    }
//
//    public long getDelayedStart() {
//        return this.timer.getDelayedStart();
//    }
//
//    public void setInterval(int interval) {
//        this.interval = interval;
//    }
//
//    public void setRunningPlan(RunningPlan runningPlan) {
//        if (CommonUtils.B_DEBUG_debug)  CommonUtils.aboutCallNotification();
//        this.runningPlan = runningPlan;
//    }
//
//    public void setTrigger(Trigger behaviourTrigger) {
//        CommonUtils.aboutCallNotification();
//        this.behaviourTrigger = behaviourTrigger;
//        this.behaviourTrigger.addConditionVariable(this.runCV);
//    }
//
//    public boolean isFinished() {
//        return finished;
//    }
//
//    public void notifyLoopFinished() {
//        this.finished = !this.loop;
//    }
//
//    public void isLoop(boolean loop) {
//        this.loop = loop;
//    }
}
