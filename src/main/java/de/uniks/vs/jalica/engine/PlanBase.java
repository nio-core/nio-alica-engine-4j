package de.uniks.vs.jalica.engine;

//TODO: mutex check

import de.uniks.vs.jalica.common.utils.CommonUtils;
import de.uniks.vs.jalica.common.ConditionVariable;
import de.uniks.vs.jalica.engine.common.SystemConfig;
import de.uniks.vs.jalica.engine.containers.messages.AlicaEngineInfo;
import de.uniks.vs.jalica.engine.model.*;
import de.uniks.vs.jalica.engine.planselection.PlanSelector;
import de.uniks.vs.jalica.engine.authority.AuthorityManager;
import de.uniks.vs.jalica.engine.syncmodule.SyncModule;
import de.uniks.vs.jalica.engine.teammanagement.TeamObserver;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by alex on 13.07.17.
 * Updated 22.6.19
 */
public class PlanBase implements Runnable {

    // Owning container of running plans (replace with uniqueptrs once possibe)
    ArrayList<RunningPlan> runningPlans;

    AlicaEngine ae;
    Plan masterPlan;

    TeamObserver teamObserver;
    IRoleAssignment ra;
    SyncModule syncModel;
    AuthorityManager authModul;
    IAlicaCommunication statusPublisher;
    AlicaClock alicaClock;

    RunningPlan rootNode;

    RunningPlan deepestNode;

    Thread mainThread;
    Logger log;
    AlicaEngineInfo statusMessage;

    AlicaTime loopTime;
    AlicaTime lastSendTime;
    AlicaTime minSendInterval;
    AlicaTime maxSendInterval;
    AlicaTime loopInterval;
    AlicaTime lastSentStatusTime;
    AlicaTime sendStatusInterval;

    Lock lomutex;
    Lock stepMutex;

    ArrayList<RunningPlan> fpEvents;
    ConditionVariable fpEventWait;
    ConditionVariable stepModeCV;
    RuleBook ruleBook;

    int treeDepth;
    boolean running;
    boolean sendStatusMessages;
    boolean isWaiting;

    public PlanBase(AlicaEngine ae, Plan masterPlan) {
        this.ae = ae;
        this.masterPlan = masterPlan;
        this.teamObserver = ae.getTeamObserver();
        this.ra = ae.getRoleAssignment();
        this.syncModel = ae.getSyncModul();
        this.authModul = ae.getAuth();
        this.statusPublisher = null;
        this.alicaClock = ae.getAlicaClock();
        this.rootNode = null;
        this.deepestNode = null;
        this.mainThread = null;
        this.log = ae.getLog();
        this.statusMessage = null;
        this.stepModeCV = new ConditionVariable(this);
        this.fpEventWait = new ConditionVariable(this);
        this.ruleBook = new RuleBook(ae, this);
        this.treeDepth = 0;
        this.running = false;
        this.isWaiting = false;

        this.runningPlans = new ArrayList<>();

        this.lastSendTime = new AlicaTime();
        this.lastSentStatusTime = new AlicaTime();

        this.lomutex = new ReentrantLock();
        this.stepMutex = new ReentrantLock();

        SystemConfig sc = this.ae.getSystemConfig();

        double freq = Double.valueOf((String) sc.get("Alica").get("Alica.EngineFrequency"));
        double minbcfreq = Double.valueOf((String) sc.get("Alica").get("Alica.MinBroadcastFrequency"));
        double maxbcfreq = Double.valueOf((String) sc.get("Alica").get("Alica.MaxBroadcastFrequency"));

        if (freq > 1000) {
            System.out.println("PB: ALICA should not be used with more than 1000Hz");
        }

        if (maxbcfreq > freq) {
            System.out.println("PB: Alica.conf: Maximum broadcast frequency must not exceed the engine frequency");
        }

        if (minbcfreq > maxbcfreq) {
            System.out.println("PB: Alica.conf: Minimal broadcast frequency must be lower or equal teamObserver maximal broadcast frequency!");
        }

        this.loopTime = new AlicaTime().inSeconds(1.0 / freq);
        this.minSendInterval = new AlicaTime().inSeconds(1.0 / maxbcfreq);
        this.maxSendInterval = new AlicaTime().inSeconds(1.0 / minbcfreq);

        long halfLoopTime = this.loopTime.time / 2;

        this.sendStatusMessages = Boolean.valueOf((String) sc.get("Alica").get("Alica.StatusMessages.Enabled"));

        if (sendStatusMessages) {
            double stfreq = Double.valueOf((String) sc.get("Alica").get("Alica.StatusMessages.Frequency"));

            if (stfreq > freq) {
                System.out.println("PB: Alica.conf: Status messages frequency must not exceed the engine frequency");
            }
            this.sendStatusInterval = new AlicaTime().inSeconds(1.0 / stfreq);
            this.statusMessage = new AlicaEngineInfo();
            this.statusMessage.senderID = this.ae.getTeamManager().getLocalAgentID();
            this.statusMessage.masterPlan = masterPlan.getName();
        }

        if (CommonUtils.PB_DEBUG_debug) System.out.println("PB: Engine loop time is " + (loopTime.inMilliseconds())
                + "ms, broadcast interval is " + (this.minSendInterval.inMilliseconds())
                + "ms - " + (this.maxSendInterval.inMilliseconds()) + "ms");

        if (halfLoopTime < this.minSendInterval.time) {
            this.minSendInterval.time -= halfLoopTime;
            this.maxSendInterval.time -= halfLoopTime;
        }
    }

    public void start() {

        if (this.ae.getAlicaClock() == null) {
            System.out.println("PB: Start impossible, without ALICA Clock set!");
        }

        if (!this.running) {
            this.running = true;
            this.mainThread = new Thread(this);
            this.mainThread.start();
        }
    }

    public void run() {
        System.out.println("PB: Run-Method of PlanBase started. ");

        while (this.running) {
            AlicaTime beginTime = this.alicaClock.now();
            this.log.itertionStarts();

            if (this.ae.getStepEngine()) {

                if (CommonUtils.PB_DEBUG_debug) {
                    if (CommonUtils.PB_DEBUG_debug) System.out.println("PB: ===CUR TREE===");
                    if (this.rootNode == null) {
                        if (CommonUtils.PB_DEBUG_debug) System.out.println("PB: NULL");
                    } else {
                        this.rootNode.printRecursive();
                    }
                    if (CommonUtils.PB_DEBUG_debug) System.out.println("PB: ===END CUR TREE===");
                }
                {
//                    std::unique_lock < std::mutex > lckStep(this.stepMutex);
                    Lock lckStep = this.stepMutex;
                    this.isWaiting = true;
                    AlicaEngine ae = this.ae;
//                    this.stepModeCV.wait(lckStep,[ae] { return ae.getStepCalled(); });
                    this.stepModeCV.cvWait(lckStep, this, () -> this.ae.getStepCalled());
                    this.ae.setStepCalled(false);
                    this.isWaiting = false;
                    if (!this.running) {
                        return;
                    }
                }
                beginTime = this.alicaClock.now();
            }

            // Send tick to other modules
            //_ae.getCommunicator().tick(); // not implemented as ros works asynchronous
            this.teamObserver.tick(this.rootNode);
            this.ra.tick();
            this.syncModel.tick();
            this.authModul.tick(this.rootNode);

            if (this.rootNode == null) {
                this.rootNode = this.ruleBook.initialisationRule(this.masterPlan);
            }
            this.rootNode.preTick();
            if (this.rootNode.tick(this.ruleBook) == PlanChange.FailChange) {
                System.out.println("PB: MasterPlan Failed");
            }
            // clear deepest node pointer before deleting plans:
            if (this.deepestNode != null && this.deepestNode.isRetired()) {
                this.deepestNode = null;
            }
            // remove deletable plans:
            // this should be done just before clearing fpEvents, to make sure no spurious pointers remain
            if (CommonUtils.PB_DEBUG_debug) {
                int retiredCount = 0;
                int inActiveCount = 0;
                int deleteCount = 0;
                int totalCount = this.runningPlans.size();
                for (int i = this.runningPlans.size() - 1; i >= 0; --i) {
                    if (this.runningPlans.get(i).isRetired()) {
                        ++retiredCount;
                    } else if (!this.runningPlans.get(i).isActive()) {
                        ++inActiveCount;
                    }
                    if (this.runningPlans.get(i).isDeleteable()) {
                        this.runningPlans.remove(this.runningPlans.get(i));
                        ++deleteCount;
                    }
                }
                System.out.println("PB: " + (totalCount - inActiveCount - retiredCount) + " active " + retiredCount + " retired " + inActiveCount
                        + " inactive deleted: " + deleteCount);

            } else {
                for (int i = this.runningPlans.size() - 1; i >= 0; --i) {
                    if (this.runningPlans.get(i).isDeleteable()) {
                        this.runningPlans.remove(this.runningPlans.get(i));
                    }
                }
            }
            // lock for fpEvents
            {
//                std::lock_guard<std::mutex> lock(this.lomutex);
                synchronized (this) {
                    this.fpEvents = new ArrayList<>();
                }
            }

            AlicaTime now = this.alicaClock.now();

            if (now.time < this.lastSendTime.time) {
                CommonUtils.aboutWarning("PB: lastSendTime is in the future of the current system time, did the system time change?");
                this.lastSendTime = now;
            }

            if ((this.ruleBook.hasChangeOccurred() && this.lastSendTime.time + this.minSendInterval.time < now.time) || this.lastSendTime.time + this.maxSendInterval.time < now.time) {
                ArrayList<Long> msg = new ArrayList<>();
                this.deepestNode = this.rootNode;
                this.treeDepth = 0;
                this.rootNode.toMessage(msg, this.deepestNode, this.treeDepth, 0);
                this.teamObserver.doBroadCast(msg);
                this.lastSendTime = now;
                this.ruleBook.resetChangeOccurred();
            }

            if (this.sendStatusMessages && this.lastSentStatusTime.time + this.sendStatusInterval.time < this.alicaClock.now().time) {
                if (this.deepestNode != null) {
                    this.statusMessage.agentIDsWithMe.clear();
                    this.statusMessage.currentPlan = this.deepestNode.getActivePlan().getName();
                    if (this.deepestNode.getActiveEntryPoint() != null) {
                        this.statusMessage.currentTask = this.deepestNode.getActiveEntryPoint().getTask().getName();
                    } else {
                        this.statusMessage.currentTask = "IDLE";
                    }
                    if (this.deepestNode.getActiveState() != null) {
                        this.statusMessage.currentState = this.deepestNode.getActiveState().getName();
                        this.deepestNode.getAssignment().getAgentsInState(this.deepestNode.getActiveState(), this.statusMessage.agentIDsWithMe);

                    } else {
                        this.statusMessage.currentState = "NONE";
                    }
                    Role tmpRole = this.ra.getOwnRole();
                    if (tmpRole != null) {
                        this.statusMessage.currentRole = this.ra.getOwnRole().getName();
                    } else {
                        this.statusMessage.currentRole = "No Role";
                    }
                    this.ae.getCommunicator().sendAlicaEngineInfo(this.statusMessage);
                    this.lastSentStatusTime = this.alicaClock.now();
                }
            }

            this.log.iterationEnds(this.rootNode);

            this.ae.iterationComplete();

            now = this.alicaClock.now();

            AlicaTime availTime = new AlicaTime(this.loopTime.time - (now.time - beginTime.time));
            boolean checkFp = false;
            if (availTime.time > AlicaTime.milliseconds(1)) {
//                std::unique_lock<std::mutex> lock(this.lomutex);
                Lock lock = this.lomutex;
                lock.lock();
                boolean result = this.fpEventWait.cvWaitFor(lock, availTime.inNanoseconds());
                checkFp = !this.fpEventWait.isInterrupted() == result;
                lock.unlock();
            }

            if (checkFp && this.fpEvents.size() > 0) {
                // lock for fpEvents
//                std::lock_guard<std::mutex> lock(this.lomutex);
                synchronized (this.fpEvents) {
                    while (this.running && availTime.time > AlicaTime.milliseconds(1) && this.fpEvents.size() > 0) {
                        RunningPlan rp = this.fpEvents.get(0);
                        this.fpEvents.remove(0);

                        if (rp.isActive()) {
                            boolean first = true;
                            while (rp != null) {
                                PlanChange change = this.ruleBook.visit(rp);
                                if (!first && change == PlanChange.NoChange) {
                                    break;
                                }
                                rp = rp.getParent();
                                first = false;
                            }
                        }
                        now = this.alicaClock.now();
                        availTime.time = this.loopTime.time - (now.time - beginTime.time);
                    }
                }
            }

            now = this.alicaClock.now();
            availTime.time = this.loopTime.time - (now.time - beginTime.time);

            System.out.println("PB: availTime " + availTime);

            if (availTime.time > AlicaTime.microseconds(100) && !this.ae.getStepEngine()) {
                this.alicaClock.sleep(availTime);
            }
        }
    }

    /**
     * Stops the plan base thread.
     */
    void stop() {
        this.running = false;
        this.ae.setStepCalled(true);

        if (this.ae.getStepEngine()) {
            this.ae.setStepCalled(true);
            this.stepModeCV.notifyOneThread();
        }

        if (this.mainThread != null) {
            this.mainThread.stop();
        }
        this.mainThread = null;
    }


    void addFastPathEvent(RunningPlan p) {
        {
//            std::lock_guard<std::mutex> lock(this.lomutex);
            synchronized (this.fpEvents) {
                this.fpEvents.add(p);
            }
        }
        this.fpEventWait.notifyAllThreads();
    }

    AlicaTime getloopInterval() {
        return this.loopInterval;
    }

    void setLoopInterval(AlicaTime loopInterval) {
        this.loopInterval = loopInterval;
    }

    ConditionVariable getStepModeCV() {
        if (!this.ae.getStepEngine()) {
            return null;
        }
        return this.stepModeCV;
    }

    /**
     * Returns the deepest ALICA node
     */
    RunningPlan getDeepestNode() {
        return this.deepestNode;
    }

    public RunningPlan getRootNode()  { return this.runningPlans.isEmpty() ? null : this.runningPlans.get(0); }
    public PlanSelector getPlanSelector()  { return this.ruleBook.getPlanSelector(); }

    public Plan getMasterPlan()  { return this.masterPlan; }
    public boolean isWaiting()  { return this.isWaiting; }

    public RunningPlan makeRunningPlan( Plan plan) {
        this.runningPlans.add(new RunningPlan(this.ae, plan));
        return this.runningPlans.get(this.runningPlans.size()-1);
    }

    public RunningPlan makeRunningPlan( Behaviour beh) {
        this.runningPlans.add(new RunningPlan(this.ae, beh));
        return this.runningPlans.get(this.runningPlans.size()-1);
    }

    public RunningPlan makeRunningPlan(PlanType pt) {
        this.runningPlans.add(new RunningPlan(this.ae, pt));
        return this.runningPlans.get(this.runningPlans.size()-1);
    }

//    public void run() {
//        if (CommonUtils.PB_DEBUG_debug) System.out.println("PB: Run-Method of PlanBase started. " );
//
//        while (this.running) {
//            AlicaTime beginTime = alicaClock.now();
//            this.log.itertionStarts();
//
//// TODO: implement step engine part
//            if (alicaEngine.getStepEngine()) {
//                if (CommonUtils.PB_DEBUG_debug) System.out.println("PB: ===CUR TREE===");
//
//                if (this.rootNode == null)
//                    if (CommonUtils.PB_DEBUG_debug) System.out.println( "PB: NULL" );
//				else
//				    if (CommonUtils.PB_DEBUG_debug) rootNode.printRecursive();
//                if (CommonUtils.PB_DEBUG_debug) System.out.println( "PB: ===END CUR TREE===" );
//
////              // TODO fix implementation
//                Thread condition = new Thread() {
//                    @Override
//                    public void run() {
//
//                            synchronized (this) {
//                                try {
//                                    Thread.sleep(100);
//                                } catch (InterruptedException e) {
//                                    e.printStackTrace();
//                                }
//
//                                if (alicaEngine.getStepCalled()) {
//                                    if (CommonUtils.PB_DEBUG_debug) System.out.println("PB: called");
//                                    stepModeCV.notifyOneThread();
//                                }
//                            }
//                    }
//                };
//                condition.start();
//
//                synchronized (this) {
//                    try {
//                       this.wait();
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//                if (CommonUtils.PB_DEBUG_debug) System.out.println("PB: step engine awakened");
//                this.alicaEngine.setStepCalled(false);
//                if (!running) {
//                    return;
//                }
//
//                beginTime = alicaClock.now();
//            }
//
//            //Send tick teamObserver other modules
////            System.out.println(Thread.currentThread().getId());
//            try {
//                this.alicaEngine.getCommunicator().tick();
//            }
//            catch (NullPointerException e) {
//                e.printStackTrace();
//            }
//            this.teamObserver.tick(this.rootNode);
//            this.roleAssignment.tick();
//            this.syncModel.tick();
//            this.authorityManager.tick(this.rootNode);
//
//            if (this.rootNode == null) {
//                this.rootNode = ruleBook.initialisationRule(this.masterPlan);
//            }
//            this.rootNode.preTick();
//
//            if (this.rootNode.tick(this.ruleBook) == PlanChange.FailChange) {
//                System.err.println("PB: MasterPlan Failed");
//            }
//
//            //lock for runningPlans
//            synchronized (this.runningPlans) {
////                lock_guard<mutex> lock(lomutex);
//                this.runningPlans = new ConcurrentLinkedDeque<>();
//            }
//            AlicaTime now = alicaClock.now();
//
////            if (CommonUtils.PB_DEBUG_debug) System.out.println(" PB: " + now.time + " > " +this.lastSendTime.time + " " +  (now.time - this.lastSendTime.time));
//            if (CommonUtils.PB_DEBUG_debug) System.out.println("PB: delay time " + (now.time - this.lastSendTime.time)+"ns");
//
//            if (now.time <= this.lastSendTime.time) {
//                // Taker fix
//                System.err.println("PB: lastSendTime is in the future of the current system time, did the system time change?" );
//                this.lastSendTime.time = now.time;
//            }
//
//            if ((this.ruleBook.isChangeOccured() && (this.lastSendTime.time + this.minSendInterval.time) < now.time)
//					|| (this.lastSendTime.time + this.maxSendInterval.time) < now.time) {
//                ArrayList<Long> msg = new ArrayList<>();
//                this.deepestNode = this.rootNode;
//                this.treeDepth = 0;
//                this.rootNode.toMessage(msg, this.deepestNode, this.treeDepth, 0);
//                this.teamObserver.doBroadCast(msg);
//                this.lastSendTime = now;
//                this.ruleBook.setChangeOccured(false);
//            }
//
//            if (this.sendStatusMessages && (this.lastSentStatusTime.time + this.sendStatusInterval.time) < alicaClock.now().time) {
//
//                if (this.deepestNode != null) {
//                    this.statusMessage.agentIDsWithMe.clear();
//                    this.statusMessage.currentPlan = this.deepestNode.getPlan().getName();
//
//                    if (this.deepestNode.getOwnEntryPoint() != null) {
//                        this.statusMessage.currentTask = this.deepestNode.getOwnEntryPoint().getTask().getName();
//                    }
//					else {
//                        this.statusMessage.currentTask = "IDLE";
//                    }
//
//                    if (this.deepestNode.getActiveState() != null) {
//                        this.statusMessage.currentState = this.deepestNode.getActiveState().getName();
//                        Set<Long> agentsInState = this.deepestNode.getAssignment().getAgentStateMapping().getAgentsInState(this.deepestNode.getActiveState());
//                        System.out.println("BP: AGENT:" +this.statusMessage.senderID+ "    AGENTS :"+agentsInState);
//                        CommonUtils.copy( this.deepestNode.getAssignment().getAgentStateMapping().getAgentsInState(this.deepestNode.getActiveState()),
//                                    0,
//                                     this.deepestNode.getAssignment().getAgentStateMapping().getAgentsInState(this.deepestNode.getActiveState()
////                                               ).size()-1, back_inserter(this.statusMessage.agentIDsWithMe)
//                                          ).size()-1,
//                                         (this.statusMessage.agentIDsWithMe));
//                    }
//					else {
//                        this.statusMessage.currentState = "NONE";
//                    }
//                    this.statusMessage.currentRole = this.roleAssignment.getOwnRole().getName();
//                    alicaEngine.getCommunicator().sendAlicaEngineInfo(this.statusMessage);
//                    this.lastSentStatusTime = alicaClock.now();
//                }
//            }
//
//            this.log.iterationEnds(this.rootNode);
//            this.alicaEngine.iterationComplete();
//            now = alicaClock.now();
//
//            long availTime = this.loopTime.time - now.time - beginTime.time;
////            availTime = availTime < 0? 0: availTime;
//
//            if (runningPlans.size() > 0) {
//                //lock for runningPlans
//                synchronized (this.runningPlans) {
//
//                    while (this.running && availTime > AlicaTime.milliseconds(1) && runningPlans.size() > 0) {
////                        RunningPlan runningPlan = runningPlans.peek();//front();
////                        runningPlans.poll();
//                        RunningPlan runningPlan = runningPlans.poll();
//
//                        if (runningPlan.isActive()) {
//                            boolean first = true;
//
//                            while (runningPlan != null) {
//                                System.out.println( "PB: TICK FPEVENT " );
//                                PlanChange change = this.ruleBook.visit(runningPlan);
//                                System.out.println("PB: AFTER TICK FPEVENT " );
//                                if (!first && change == PlanChange.NoChange)
//                                {
//                                    break;
//                                }
//                                runningPlan = runningPlan.getParent();
//                                first = false;
//                            }
//                        }
//                        now = alicaClock.now();
//                        availTime = this.loopTime.time - (now.time - beginTime.time);
//                    }
//                }
//            }
//
//            now = alicaClock.now();
//            availTime = this.loopTime.time - (now.time - beginTime.time);
//
//            if (CommonUtils.PB_DEBUG_debug) System.out.println("PB: availTime " + availTime );
//            if (CommonUtils.PB_DEBUG_debug) System.out.println("PB: loopTime " + this.loopTime.time );
//            if (CommonUtils.PB_DEBUG_debug) System.out.println("PB: duration time  " + (now.time - beginTime.time));
//
//            if (availTime > AlicaTime.microseconds(100) && !alicaEngine.getStepEngine()) {
//
//                if (CommonUtils.PB_DEBUG_debug) System.out.println("PB: sleep " + availTime);
//                alicaClock.sleep(availTime);
//            }
//
//            if (CommonUtils.PB_DEBUG_debug) System.out.println("PB: loop const " + this.loopTime.time
//                    + "ns    loop duration time  " + (alicaClock.now().time - beginTime.time)+"ns");
////            double durarion = alicaClock.now().time - beginTime.time;
////            double rest = this.loopTime.time - durarion;
////            if (CommonUtils.PB_DEBUG_debug) System.out.println("PB: DIFF " + rest);
//            if (CommonUtils.PB_DEBUG_debug) System.out.println("-------------------------");
//        }
//    }
//
//
//    public void addFastPathEvent(RunningPlan runningPlan) {
//        synchronized (this.runningPlans) {
//            if (CommonUtils.PB_DEBUG_debug) System.out.println("PB: new event " + runningPlan.getPlan().getName());
//            runningPlans.add(runningPlan);
//        }
//    }
//
//    public ConditionVariable getStepModeCV() {
//        return stepModeCV;
//    }
//
//    public RunningPlan getRootNode() { return rootNode; }
//
//    public AlicaTime getLoopInterval() {
//        return loopInterval;
//    }
//
//    public void setLoopInterval(AlicaTime loopInterval) {
//        this.loopInterval = loopInterval;
//    }
//
//    public RunningPlan getDeepestNode() {
//        return deepestNode;
//    }
//
//    public RunningPlan makeRunningPlan(Plan plan) {
//        RunningPlan runningPlan = new RunningPlan(alicaEngine, plan);
//        runningPlans.add(runningPlan);
//        return runningPlan;
//    }
//
//    public RunningPlan makeRunningPlan(PlanType planType) {
//        RunningPlan runningPlan = new RunningPlan(alicaEngine, planType);
//        runningPlans.add(runningPlan);
//        return runningPlan;
//    }
//
//    public RunningPlan makeRunningPlan(BehaviourConfiguration behaviourConfiguration) {
//        RunningPlan runningPlan = new RunningPlan(alicaEngine, behaviourConfiguration);
//        runningPlans.add(runningPlan);
//        return runningPlan;
//    }
}

