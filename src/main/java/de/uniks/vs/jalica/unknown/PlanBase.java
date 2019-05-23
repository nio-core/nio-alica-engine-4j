package de.uniks.vs.jalica.unknown;

//TODO: mutex check

import de.uniks.vs.jalica.common.Logger;
import de.uniks.vs.jalica.engine.AlicaEngine;
import de.uniks.vs.jalica.teamobserver.ITeamObserver;
import de.uniks.vs.jalica.unknown.Communication.AlicaEngineInfo;

import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * Created by alex on 13.07.17.
 */
public class PlanBase implements Runnable {

    private ConditionVariable stepModeCV;
    private  AlicaEngine ae;
    private  Plan masterPlan;

    private RunningPlan rootNode;
    private RunningPlan deepestNode;
    private int treeDepth;
    private RuleBook ruleBook;
    private ITeamObserver teamObserver;
    private IRoleAssignment roleAssignment;
    private ISyncModul syncModel;
    private AuthorityManager authModul;
    private AlicaCommunication statusPublisher;
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
    private Logger log;

    private AlicaEngineInfo statusMessage;
    private PriorityQueue<RunningPlan> fpEvents;

    public PlanBase(AlicaEngine ae, Plan masterPlan) {

        this.ae = ae;
        this.masterPlan = masterPlan;
        this.rootNode = null;
        this.mainThread = Thread.currentThread();
        this.teamObserver = ae.getTeamObserver();
        this.syncModel = ae.getSyncModul();
        this.authModul = ae.getAuthorityManager();
        this.roleAssignment = ae.getRoleAssignment();
        this.ruleBook = new RuleBook(ae);
        this.alicaClock = ae.getAlicaClock();
        this.lastSendTime = new AlicaTime();
        this.lastSentStatusTime = new AlicaTime();
        this.loopInterval = new AlicaTime();
        this.log = ae.getLogger();
        this.fpEvents = new PriorityQueue<>();
        this.treeDepth = 0;
        this.statusPublisher = null;
        this.deepestNode = null;
        this.stepModeCV = null;
        this.running = false;

        double frequency      = Double.valueOf((String) this.ae.getSystemConfig().get("Alica").get("Alica.EngineFrequency"));
        double minBroadcastFrequency = Double.valueOf((String) this.ae.getSystemConfig().get("Alica").get("Alica.MinBroadcastFrequency"));
        double maxBroadcastFrequency = Double.valueOf((String) this.ae.getSystemConfig().get("Alica").get("Alica.MaxBroadcastFrequency"));

        if (frequency > 1000000) {
            System.out.println("PB: ALICA should not be used with more than 1000Hz");
        }

        if (maxBroadcastFrequency > frequency) {
            ae.abort("PB: Alica.conf: Maximum broadcast frequency must not exceed the engine frequency");
        }

        if (minBroadcastFrequency > maxBroadcastFrequency) {
            ae.abort( "PB: Alica.conf: Minimal broadcast frequency must be lower or equal to maximal broadcast frequency!");
        }

//        this.loopTime        = new AlicaTime(Math.max(1000000, Math.round(1.0 / frequency *      1000000000)));
//        this.minSendInterval = new AlicaTime(Math.max(1000000, Math.round(1.0 / maxBroadcastFrequency * 1000000000)));
//        this.maxSendInterval = new AlicaTime(Math.max(1000000, Math.round(1.0 / minBroadcastFrequency * 1000000000)));
        this.loopTime        = new AlicaTime().inSeconds(1.0 / frequency);
        this.minSendInterval = new AlicaTime().inSeconds(1.0 / maxBroadcastFrequency);
        this.maxSendInterval = new AlicaTime().inSeconds(1.0 / minBroadcastFrequency);

        long halfLoopTime = this.loopTime.time / 2;

        this.sendStatusMessages = Boolean.valueOf((String) this.ae.getSystemConfig().get("Alica").get("Alica.StatusMessages.Enabled"));

        if (sendStatusMessages) {
            double statusMessageFrequency = Double.valueOf((String) this.ae.getSystemConfig().get("Alica").get("Alica.StatusMessages.Frequency"));

            if (statusMessageFrequency > frequency) {
                ae.abort("PB: Alica.conf: Status messages frequency must not exceed the engine frequency");
            }
            this.sendStatusInterval = new AlicaTime().inSeconds(1.0 / statusMessageFrequency);
            this.statusMessage = new AlicaEngineInfo();
            this.statusMessage.senderID = this.teamObserver.getOwnID();
            this.statusMessage.masterPlan = masterPlan.getName();
        }

        if (this.ae.getStepEngine()) {
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

        if (ae.getAlicaClock() == null) {
            ae.abort("PB: Start impossible, without ALICA Clock set!");
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
            if (ae.getStepEngine()) {
                if (CommonUtils.PB_DEBUG_debug) System.out.println("PB: ===CUR TREE===");

                if (this.rootNode == null)
                    if (CommonUtils.PB_DEBUG_debug) System.out.println( "PB: NULL" );
				else
				    if (CommonUtils.PB_DEBUG_debug) rootNode.printRecursive();
                if (CommonUtils.PB_DEBUG_debug) System.out.println( "PB: ===END CUR TREE===" );
////#endif
//                {
//                    // TODO fix implementation
//                    unique_lock<mutex> lckStep(stepMutex);
//                    stepModeCV.wait(
//                            {
//                        return this.ae.getStepCalled();
//                    }
//                    );

                Thread condition = new Thread() {
                    @Override
                    public void run() {

                            synchronized (this) {
                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                                if (ae.getStepCalled()) {
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
                this.ae.setStepCalled(false);
                if (!running) {
                    return;
                }

                beginTime = alicaClock.now();
            }

            //Send tick to other modules
//            System.out.println(Thread.currentThread().getId());
            try {
                this.ae.getCommunicator().tick();
            }
            catch (NullPointerException e) {
                e.printStackTrace();
            }
            this.teamObserver.tick(this.rootNode);
            this.roleAssignment.tick();
            this.syncModel.tick();
            this.authModul.tick(this.rootNode);

            if (this.rootNode == null) {
                this.rootNode = ruleBook.initialisationRule(this.masterPlan);
            }

            PlanChange result = this.rootNode.tick(this.ruleBook);

            if (result == PlanChange.FailChange) {
                System.err.println("PB: MasterPlan Failed");
            }

            //lock for fpEvents
            synchronized (this.fpEvents) {
//                lock_guard<mutex> lock(lomutex);
                this.fpEvents = new PriorityQueue<>();
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
                    ae.getCommunicator().sendAlicaEngineInfo(this.statusMessage);
                    this.lastSentStatusTime = alicaClock.now();
                }
            }

            this.log.iterationEnds(this.rootNode);
            this.ae.iterationComplete();
            now = alicaClock.now();

            long availTime = this.loopTime.time - now.time - beginTime.time;
//            availTime = availTime < 0? 0: availTime;

            if (fpEvents.size() > 0) {
                //lock for fpEvents
                synchronized (this.fpEvents) {

                    while (this.running && availTime > AlicaTime.milliseconds(1) && fpEvents.size() > 0) {
//                        RunningPlan runningPlan = fpEvents.peek();//front();
//                        fpEvents.poll();
                        RunningPlan runningPlan = fpEvents.poll();

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

                        availTime = this.loopTime.time - now.time - beginTime.time;
                    }
                }
            }

            if (CommonUtils.PB_DEBUG_debug) System.out.println("PB: availTime " + availTime );
            if (CommonUtils.PB_DEBUG_debug) System.out.println("PB: duration time  " + (now.time - beginTime.time));

            if (availTime > 0 && !ae.getStepEngine()) {

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


    public ConditionVariable getStepModeCV() {
        return stepModeCV;
    }

    public void addFastPathEvent(RunningPlan runningPlan) {
        synchronized (this.fpEvents) {
            if (CommonUtils.PB_DEBUG_debug) System.out.println("PB: new event " + runningPlan.getPlan().getName());
            fpEvents.add(runningPlan);
        }
    }

    public RunningPlan getRootNode() { return rootNode; }
}
