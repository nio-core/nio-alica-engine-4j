package de.uniks.vs.jalica.engine;

//TODO: mutex check

import de.uniks.vs.jalica.common.utils.CommonUtils;
import de.uniks.vs.jalica.common.ConditionVariable;
import de.uniks.vs.jalica.engine.containers.messages.AlicaEngineInfo;
import de.uniks.vs.jalica.engine.model.Behaviour;
import de.uniks.vs.jalica.engine.model.BehaviourConfiguration;
import de.uniks.vs.jalica.engine.model.Plan;
import de.uniks.vs.jalica.engine.model.PlanType;
import de.uniks.vs.jalica.engine.syncmodule.ISyncModule;
import de.uniks.vs.jalica.engine.authority.AuthorityManager;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Created by alex on 13.07.17.
 */
public class PlanBase implements Runnable {

    private AlicaEngine alicaEngine;
    private Plan masterPlan;

    private RunningPlan rootNode;
    private RunningPlan deepestNode;
    private int treeDepth;
    private RuleBook ruleBook;
    private ITeamObserver teamObserver;
    private IRoleAssignment roleAssignment;
    private ISyncModule syncModel;
    private AuthorityManager authorityManager;
//    private AlicaCommunication statusPublisher;
    private AlicaClock alicaClock;

    private AlicaTime loopTime;
    private AlicaTime lastSendTime;
    private AlicaTime minSendInterval;
    private AlicaTime maxSendInterval;
    private AlicaTime loopInterval;
    private AlicaTime lastSentStatusTime;
    private AlicaTime sendStatusInterval;

    private boolean running;
    private boolean sendStatusMessages;

    private Thread mainThread;
    private ConditionVariable stepModeCV;
    private Logger log;

    private AlicaEngineInfo statusMessage;
//    private PriorityQueue<RunningPlan> runningPlans;
    private ConcurrentLinkedDeque<RunningPlan> runningPlans;

    public PlanBase(AlicaEngine ae, Plan masterPlan) {

        this.alicaEngine = ae;
        this.masterPlan = masterPlan;
        this.rootNode = null;
        this.mainThread = Thread.currentThread();
        this.teamObserver = ae.getTeamObserver();
        this.syncModel = ae.getSyncModul();
        this.authorityManager = ae.getAuthorityManager();
        this.roleAssignment = ae.getRoleAssignment();
        this.ruleBook = new RuleBook(ae);

        this.alicaClock = ae.getAlicaClock();
        this.lastSendTime = new AlicaTime();
        this.lastSentStatusTime = new AlicaTime();
//        this.loopInterval = new AlicaTime();
        this.log = ae.getLogger();
        this.runningPlans = new ConcurrentLinkedDeque<>();
        this.treeDepth = 0;
//        this.statusPublisher = null;
        this.deepestNode = null;
        this.stepModeCV = null;
        this.running = false;

        double frequency      = Double.valueOf((String) this.alicaEngine.getSystemConfig().get("Alica").get("Alica.EngineFrequency"));
        double minBroadcastFrequency = Double.valueOf((String) this.alicaEngine.getSystemConfig().get("Alica").get("Alica.MinBroadcastFrequency"));
        double maxBroadcastFrequency = Double.valueOf((String) this.alicaEngine.getSystemConfig().get("Alica").get("Alica.MaxBroadcastFrequency"));

        if (frequency > 1000000) {
            System.out.println("PB: ALICA should not be used with more than 1000Hz");
        }

        if (maxBroadcastFrequency > frequency) {
            ae.abort("PB: Alica.conf: Maximum broadcast frequency must not exceed the engine frequency");
        }

        if (minBroadcastFrequency > maxBroadcastFrequency) {
            ae.abort( "PB: Alica.conf: Minimal broadcast frequency must be lower or equal teamObserver maximal broadcast frequency!");
        }

//        this.loopTime        = new AlicaTime(Math.max(1000000, Math.round(1.0 / frequency *      1000000000)));
//        this.minSendInterval = new AlicaTime(Math.max(1000000, Math.round(1.0 / maxBroadcastFrequency * 1000000000)));
//        this.maxSendInterval = new AlicaTime(Math.max(1000000, Math.round(1.0 / minBroadcastFrequency * 1000000000)));
        this.loopTime        = new AlicaTime().inSeconds(1.0 / frequency);
        this.minSendInterval = new AlicaTime().inSeconds(1.0 / maxBroadcastFrequency);
        this.maxSendInterval = new AlicaTime().inSeconds(1.0 / minBroadcastFrequency);

        long halfLoopTime = this.loopTime.time / 2;

        this.sendStatusMessages = Boolean.valueOf((String) this.alicaEngine.getSystemConfig().get("Alica").get("Alica.StatusMessages.Enabled"));

        if (sendStatusMessages) {
            double statusMessageFrequency = Double.valueOf((String) this.alicaEngine.getSystemConfig().get("Alica").get("Alica.StatusMessages.Frequency"));

            if (statusMessageFrequency > frequency) {
                ae.abort("PB: Alica.conf: Status messages frequency must not exceed the engine frequency");
            }
            this.sendStatusInterval = new AlicaTime().inSeconds(1.0 / statusMessageFrequency);
            this.statusMessage = new AlicaEngineInfo();
            this.statusMessage.senderID = this.teamObserver.getOwnID();
            this.statusMessage.masterPlan = masterPlan.getName();
        }

        if (this.alicaEngine.getStepEngine()) {
            this.stepModeCV = new ConditionVariable(this);
        }

        if (CommonUtils.PB_DEBUG_debug) System.out.println("PB: Engine loop time is " +(loopTime.inMilliseconds())
                + "ms, broadcast interval is "+(this.minSendInterval.inMilliseconds())
                + "ms - " + (this.maxSendInterval.inMilliseconds()) + "ms" );

        if (halfLoopTime < this.minSendInterval.time) {
            this.minSendInterval.time -= halfLoopTime;
            this.maxSendInterval.time -= halfLoopTime;
        }
    }

    public void start() {

        if (alicaEngine.getAlicaClock() == null) {
            alicaEngine.abort("PB: Start impossible, without ALICA Clock set!");
        }

        if (!this.running) {
            this.running = true;
            this.mainThread = new Thread(this);
            this.mainThread.start();
        }
    }

    public void run() {
        if (CommonUtils.PB_DEBUG_debug) System.out.println("PB: Run-Method of PlanBase started. " );

        while (this.running) {
            AlicaTime beginTime = alicaClock.now();
            this.log.itertionStarts();

// TODO: implement step engine part
            if (alicaEngine.getStepEngine()) {
                if (CommonUtils.PB_DEBUG_debug) System.out.println("PB: ===CUR TREE===");

                if (this.rootNode == null)
                    if (CommonUtils.PB_DEBUG_debug) System.out.println( "PB: NULL" );
				else
				    if (CommonUtils.PB_DEBUG_debug) rootNode.printRecursive();
                if (CommonUtils.PB_DEBUG_debug) System.out.println( "PB: ===END CUR TREE===" );

//              // TODO fix implementation
                Thread condition = new Thread() {
                    @Override
                    public void run() {

                            synchronized (this) {
                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                                if (alicaEngine.getStepCalled()) {
                                    if (CommonUtils.PB_DEBUG_debug) System.out.println("PB: called");
                                    stepModeCV.notifyOneThread();
                                }
                            }
                    }
                };
                condition.start();

                synchronized (this) {
                    try {
                       this.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (CommonUtils.PB_DEBUG_debug) System.out.println("PB: step engine awakened");
                this.alicaEngine.setStepCalled(false);
                if (!running) {
                    return;
                }

                beginTime = alicaClock.now();
            }

            //Send tick teamObserver other modules
//            System.out.println(Thread.currentThread().getId());
            try {
                this.alicaEngine.getCommunicator().tick();
            }
            catch (NullPointerException e) {
                e.printStackTrace();
            }
            this.teamObserver.tick(this.rootNode);
            this.roleAssignment.tick();
            this.syncModel.tick();
            this.authorityManager.tick(this.rootNode);

            if (this.rootNode == null) {
                this.rootNode = ruleBook.initialisationRule(this.masterPlan);
            }
            this.rootNode.preTick();

            if (this.rootNode.tick(this.ruleBook) == PlanChange.FailChange) {
                System.err.println("PB: MasterPlan Failed");
            }

            //lock for runningPlans
            synchronized (this.runningPlans) {
//                lock_guard<mutex> lock(lomutex);
                this.runningPlans = new ConcurrentLinkedDeque<>();
            }
            AlicaTime now = alicaClock.now();

//            if (CommonUtils.PB_DEBUG_debug) System.out.println(" PB: " + now.time + " > " +this.lastSendTime.time + " " +  (now.time - this.lastSendTime.time));
            if (CommonUtils.PB_DEBUG_debug) System.out.println("PB: delay time " + (now.time - this.lastSendTime.time)+"ns");

            if (now.time <= this.lastSendTime.time) {
                // Taker fix
                System.err.println("PB: lastSendTime is in the future of the current system time, did the system time change?" );
                this.lastSendTime.time = now.time;
            }

            if ((this.ruleBook.isChangeOccured() && (this.lastSendTime.time + this.minSendInterval.time) < now.time)
					|| (this.lastSendTime.time + this.maxSendInterval.time) < now.time) {
                ArrayList<Long> msg = new ArrayList<>();
                this.deepestNode = this.rootNode;
                this.treeDepth = 0;
                this.rootNode.toMessage(msg, this.deepestNode, this.treeDepth, 0);
                this.teamObserver.doBroadCast(msg);
                this.lastSendTime = now;
                this.ruleBook.setChangeOccured(false);
            }

            if (this.sendStatusMessages && (this.lastSentStatusTime.time + this.sendStatusInterval.time) < alicaClock.now().time) {

                if (this.deepestNode != null) {
                    this.statusMessage.agentIDsWithMe.clear();
                    this.statusMessage.currentPlan = this.deepestNode.getPlan().getName();

                    if (this.deepestNode.getOwnEntryPoint() != null) {
                        this.statusMessage.currentTask = this.deepestNode.getOwnEntryPoint().getTask().getName();
                    }
					else {
                        this.statusMessage.currentTask = "IDLE";
                    }

                    if (this.deepestNode.getActiveState() != null) {
                        this.statusMessage.currentState = this.deepestNode.getActiveState().getName();
                        Set<Long> agentsInState = this.deepestNode.getAssignment().getAgentStateMapping().getAgentsInState(this.deepestNode.getActiveState());
                        System.out.println("BP: AGENT:" +this.statusMessage.senderID+ "    AGENTS :"+agentsInState);
                        CommonUtils.copy( this.deepestNode.getAssignment().getAgentStateMapping().getAgentsInState(this.deepestNode.getActiveState()),
                                    0,
                                     this.deepestNode.getAssignment().getAgentStateMapping().getAgentsInState(this.deepestNode.getActiveState()
//                                               ).size()-1, back_inserter(this.statusMessage.agentIDsWithMe)
                                          ).size()-1,
                                         (this.statusMessage.agentIDsWithMe));
                    }
					else {
                        this.statusMessage.currentState = "NONE";
                    }
                    this.statusMessage.currentRole = this.roleAssignment.getOwnRole().getName();
                    alicaEngine.getCommunicator().sendAlicaEngineInfo(this.statusMessage);
                    this.lastSentStatusTime = alicaClock.now();
                }
            }

            this.log.iterationEnds(this.rootNode);
            this.alicaEngine.iterationComplete();
            now = alicaClock.now();

            long availTime = this.loopTime.time - now.time - beginTime.time;
//            availTime = availTime < 0? 0: availTime;

            if (runningPlans.size() > 0) {
                //lock for runningPlans
                synchronized (this.runningPlans) {

                    while (this.running && availTime > AlicaTime.milliseconds(1) && runningPlans.size() > 0) {
//                        RunningPlan runningPlan = runningPlans.peek();//front();
//                        runningPlans.poll();
                        RunningPlan runningPlan = runningPlans.poll();

                        if (runningPlan.isActive()) {
                            boolean first = true;

                            while (runningPlan != null) {
                                System.out.println( "PB: TICK FPEVENT " );
                                PlanChange change = this.ruleBook.visit(runningPlan);
                                System.out.println("PB: AFTER TICK FPEVENT " );
                                if (!first && change == PlanChange.NoChange)
                                {
                                    break;
                                }
                                runningPlan = runningPlan.getParent();
                                first = false;
                            }
                        }
                        now = alicaClock.now();
                        availTime = this.loopTime.time - (now.time - beginTime.time);
                    }
                }
            }

            now = alicaClock.now();
            availTime = this.loopTime.time - (now.time - beginTime.time);

            if (CommonUtils.PB_DEBUG_debug) System.out.println("PB: availTime " + availTime );
            if (CommonUtils.PB_DEBUG_debug) System.out.println("PB: loopTime " + this.loopTime.time );
            if (CommonUtils.PB_DEBUG_debug) System.out.println("PB: duration time  " + (now.time - beginTime.time));

            if (availTime > AlicaTime.microseconds(100) && !alicaEngine.getStepEngine()) {

                if (CommonUtils.PB_DEBUG_debug) System.out.println("PB: sleep " + availTime);
                alicaClock.sleep(availTime);
            }

            if (CommonUtils.PB_DEBUG_debug) System.out.println("PB: loop const " + this.loopTime.time
                    + "ns    loop duration time  " + (alicaClock.now().time - beginTime.time)+"ns");
//            double durarion = alicaClock.now().time - beginTime.time;
//            double rest = this.loopTime.time - durarion;
//            if (CommonUtils.PB_DEBUG_debug) System.out.println("PB: DIFF " + rest);
            if (CommonUtils.PB_DEBUG_debug) System.out.println("-------------------------");
        }
    }


    public void addFastPathEvent(RunningPlan runningPlan) {
        synchronized (this.runningPlans) {
            if (CommonUtils.PB_DEBUG_debug) System.out.println("PB: new event " + runningPlan.getPlan().getName());
            runningPlans.add(runningPlan);
        }
    }

    public ConditionVariable getStepModeCV() {
        return stepModeCV;
    }

    public RunningPlan getRootNode() { return rootNode; }

    public AlicaTime getLoopInterval() {
        return loopInterval;
    }

    public void setLoopInterval(AlicaTime loopInterval) {
        this.loopInterval = loopInterval;
    }

    public RunningPlan getDeepestNode() {
        return deepestNode;
    }

    public RunningPlan makeRunningPlan(Plan plan) {
        RunningPlan runningPlan = new RunningPlan(alicaEngine, plan);
        runningPlans.add(runningPlan);
        return runningPlan;
    }

    public RunningPlan makeRunningPlan(PlanType planType) {
        RunningPlan runningPlan = new RunningPlan(alicaEngine, planType);
        runningPlans.add(runningPlan);
        return runningPlan;
    }

    public RunningPlan makeRunningPlan(BehaviourConfiguration behaviourConfiguration) {
        RunningPlan runningPlan = new RunningPlan(alicaEngine, behaviourConfiguration);
        runningPlans.add(runningPlan);
        return runningPlan;
    }
}

