package de.uniks.vs.jalica.unknown;

//TODO: mutex check

import de.uniks.vs.jalica.common.Logger;
import de.uniks.vs.jalica.engine.AlicaEngine;
import de.uniks.vs.jalica.teamobserver.ITeamObserver;

import java.util.ArrayList;
import java.util.PriorityQueue;

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
    private IAlicaClock alicaClock;

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
//    private Mutex lomutex;
//    private Mutex stepMutex;
    private PriorityQueue<RunningPlan> fpEvents;


    public PlanBase(AlicaEngine ae, Plan masterPlan) {

        this.mainThread = Thread.currentThread();
        this.treeDepth = 0;
        this.lastSendTime = new AlicaTime(0);
        this.statusPublisher = null;
        this.lastSentStatusTime = new AlicaTime(0);
        this.loopInterval = new AlicaTime(0);
        this.deepestNode = null;
        this.log = ae.getLog();
        this.rootNode = null;
        this.masterPlan = masterPlan;
        this.ae = ae;
        this.teamObserver = ae.getTeamObserver();
        this.syncModel = ae.getSyncModul();
        this.authModul = ae.getAuth();
        this.roleAssignment = ae.getRoleAssignment();
        this.alicaClock = ae.getIAlicaClock();
        this.fpEvents = new PriorityQueue<>();

        this.ruleBook = new RuleBook(ae);
//        SystemConfig systemConfig = SystemConfig.getInstance();

        double freq = Double.valueOf((String) this.ae.getSystemConfig().get("Alica").get("Alica.EngineFrequency"));
        double minbcfreq = Double.valueOf((String) this.ae.getSystemConfig().get("Alica").get("Alica.MinBroadcastFrequency"));
        double maxbcfreq = Double.valueOf((String) this.ae.getSystemConfig().get("Alica").get("Alica.MaxBroadcastFrequency"));

        this.loopTime = new AlicaTime(Math.max(1000000, Math.round(1.0 / freq * 1000000000)));

        if (this.loopTime.time == 1000000) {
            System.out.println("PB: ALICA should not be used with more than 1000Hz . 1000Hz assumed");
        }

        if (minbcfreq > maxbcfreq) {
            ae.abort( "PB: Alica.conf: Minimal broadcast frequency must be lower or equal to maximal broadcast frequency!");
        }

        this.minSendInterval = new AlicaTime(Math.max(1000000, Math.round(1.0 / maxbcfreq * 1000000000)));
        this.maxSendInterval = new AlicaTime(Math.max(1000000, Math.round(1.0 / minbcfreq * 1000000000)));

        AlicaTime halfLoopTime = new AlicaTime(this.loopTime.time / 2);
        this.running = false;

        this.sendStatusMessages = Boolean.valueOf((String) this.ae.getSystemConfig().get("Alica").get("Alica.StatusMessages.Enabled"));

        if (sendStatusMessages) {
            double stfreq = Double.valueOf((String) this.ae.getSystemConfig().get("Alica").get("Alica.StatusMessages.Frequency"));
            this.sendStatusInterval = new AlicaTime(Math.max(1000000.0, Math.round(1.0 / stfreq * 1000000000)));
            this.statusMessage = new AlicaEngineInfo();
            this.statusMessage.senderID = this.teamObserver.getOwnID();
            this.statusMessage.masterPlan = masterPlan.getName();
        }
        this.stepModeCV = null;

        if (this.ae.getStepEngine()) {
            this.stepModeCV = new ConditionVariable(this);
        }

//#ifdef PB_DEBUG
        if (CommonUtils.PB_DEBUG_debug) System.out.println("PB: Engine loop time is " +(loopTime.time / 1000000)+ "ms, broadcast interval is "+(this.minSendInterval.time / 1000000)
                + "ms - " + (this.maxSendInterval.time / 1000000) + "ms" );
//#endif
        if (halfLoopTime.time < this.minSendInterval.time) {
            this.minSendInterval.time -= halfLoopTime.time;
            this.maxSendInterval.time -= halfLoopTime.time;
        }
    }

    public void run() {
//#ifdef PB_DEBUG
        if (CommonUtils.PB_DEBUG_debug) System.out.println("PB: Run-Method of PlanBase started. " );
//#endif
        while (this.running) {

            AlicaTime beginTime = alicaClock.now();
            this.log.itertionStarts();

// TODO: implement step engine part
            if (ae.getStepEngine()) {
////#ifdef PB_DEBUG
                if (CommonUtils.PB_DEBUG_debug) System.out.println("PB: ===CUR TREE===");

                if (this.rootNode == null) {
                    if (CommonUtils.PB_DEBUG_debug) System.out.println( "PB: NULL" );
                }
				else {
                    if (CommonUtils.PB_DEBUG_debug) rootNode.printRecursive();
                }
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
            if (CommonUtils.PB_DEBUG_debug) System.out.println("PB: delay time " + (now.time - this.lastSendTime.time));

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
                        CommonUtils.copy(this.deepestNode.getAssignment().getAgentStateMapping().getAgentsInState(
                            this.deepestNode.getActiveState()), 0,
                            this.deepestNode.getAssignment().getAgentStateMapping().getAgentsInState(
                                this.deepestNode.getActiveState()
//                            ).size()-1, back_inserter(this.statusMessage.agentIDsWithMe)
                            ).size()-1, (this.statusMessage.agentIDsWithMe)
                        );

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
            long availTime;
            now = alicaClock.now();

            if (this.loopTime.time > (now.time - beginTime.time)) {
                availTime = (long)((this.loopTime.time - (now.time - beginTime.time)) / 1000);
            }
			else {
                availTime = 0;
            }

            if (fpEvents.size() > 0) {
                //lock for fpEvents
                synchronized (this.fpEvents)
                {
//                    lock_guard<mutex> lock(lomutex);
                    while (this.running && availTime > 1000 && fpEvents.size() > 0) {
                        RunningPlan runningPlan = fpEvents.peek();//front();
                        fpEvents.poll();

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

                        if (this.loopTime.time > (now.time - beginTime.time)) {
                            availTime = (long)((this.loopTime.time - (now.time - beginTime.time)));// / 1000);
                        }
						else {
                            availTime = 0;
                        }
                    }
                }
            }

//#ifdef PB_DEBUG
            if (CommonUtils.PB_DEBUG_debug) System.out.println("PB: availTime " + availTime );
            if (CommonUtils.PB_DEBUG_debug) System.out.println("PB: duration time  " + (now.time - beginTime.time));
//#endif
            if (availTime > 0 && !ae.getStepEngine()) {
                if (CommonUtils.PB_DEBUG_debug) System.out.println("PB: sleep " + availTime);

                alicaClock.sleep(availTime);
            }

            if (CommonUtils.PB_DEBUG_debug) System.out.println("PB: loop const " + this.loopTime.time + "   loop duration time  " + (alicaClock.now().time - beginTime.time));
//            double durarion = alicaClock.now().time - beginTime.time;
//            double rest = this.loopTime.time - durarion;
//            if (CommonUtils.PB_DEBUG_debug) System.out.println("PB: DIFF " + rest);
            if (CommonUtils.PB_DEBUG_debug) System.out.println("-------------------------");
        }
    }

    public void start() {

        if (!this.running) {
            this.running = true;
            this.mainThread = new Thread(this);
            this.mainThread.start();
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
