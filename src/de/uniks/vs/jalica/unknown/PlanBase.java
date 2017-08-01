package de.uniks.vs.jalica.unknown;

//TODO: mutex check
import com.sun.corba.se.impl.orbutil.concurrent.Mutex;

import de.uniks.vs.jalica.common.Logger;
import de.uniks.vs.jalica.engine.AlicaEngine;
import de.uniks.vs.jalica.supplementary.SystemConfig;
import de.uniks.vs.jalica.teamobserver.ITeamObserver;

import java.util.ArrayList;
import java.util.PriorityQueue;

/**
 * Created by alex on 13.07.17.
 */
public class PlanBase extends Thread{
    private  condition_variable stepModeCV;
    private  AlicaEngine ae;
    private  Plan masterPlan;

    RunningPlan rootNode;
    RunningPlan deepestNode;
    int treeDepth;
    RuleBook ruleBook;
    ITeamObserver teamObserver;
    IRoleAssignment ra;
    ISyncModul syncModel;
    AuthorityManager authModul;
    IAlicaCommunication statusPublisher;
    IAlicaClock alicaClock;

    AlicaTime loopTime;
    AlicaTime lastSendTime;
    AlicaTime minSendInterval;
    AlicaTime maxSendInterval;
    AlicaTime loopInterval;
    AlicaTime lastSentStatusTime;
    AlicaTime sendStatusInterval;

    boolean running;
    boolean sendStatusMessages;

    Thread mainThread;
    Logger log;

    AlicaEngineInfo statusMessage;
    Mutex lomutex;
    Mutex stepMutex;
    private PriorityQueue<RunningPlan> fpEvents;


    public PlanBase(AlicaEngine ae, Plan masterPlan) {

        this.mainThread = null;
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
        this.ra = ae.getRoleAssignment();
        this.alicaClock = ae.getIAlicaClock();

        this.ruleBook = new RuleBook(ae);
        SystemConfig sc = SystemConfig.getInstance();

        double freq = Double.valueOf(sc.get("Alica").get("Alica.EngineFrequency"));
        double minbcfreq = Double.valueOf(sc.get("Alica").get("Alica.MinBroadcastFrequency"));
        double maxbcfreq = Double.valueOf(sc.get("Alica").get("Alica.MaxBroadcastFrequency"));
        this.loopTime = new AlicaTime(Math.max(1000000, Math.round(1.0 / freq * 1000000000)));
        if (this.loopTime.time == 1000000)
        {
            System.out.println("PB: ALICA should not be used with more than 1000Hz . 1000Hz assumed");
        }

        if (minbcfreq > maxbcfreq)
        {
            ae.abort( "PB: Alica.conf: Minimal broadcast frequency must be lower or equal to maximal broadcast frequency!");
        }

        this.minSendInterval = new AlicaTime(Math.max(1000000, Math.round(1.0 / maxbcfreq * 1000000000)));
        this.maxSendInterval = new AlicaTime(Math.max(1000000, Math.round(1.0 / minbcfreq * 1000000000)));

        AlicaTime halfLoopTime = new AlicaTime(this.loopTime.time / 2);
        this.running = false;

        this.sendStatusMessages = Boolean.valueOf(sc.get("Alica").get("Alica.StatusMessages.Enabled"));

        if (sendStatusMessages)
        {
            double stfreq = Double.valueOf(sc.get("Alica").get("Alica.StatusMessages.Frequency"));
            this.sendStatusInterval = new AlicaTime(Math.max(1000000.0, Math.round(1.0 / stfreq * 1000000000)));
            this.statusMessage = new AlicaEngineInfo();
            this.statusMessage.senderID = this.teamObserver.getOwnId();
            this.statusMessage.masterPlan = masterPlan.getName();
        }
        this.stepModeCV = null;

        if (this.ae.getStepEngine())
        {
            this.stepModeCV = new condition_variable();
        }

//#ifdef PB_DEBUG
        System.out.println("PB: Engine loop time is " +(loopTime.time / 1000000)+ "ms, broadcast interval is "+(this.minSendInterval.time / 1000000)
                + "ms - " + (this.maxSendInterval.time / 1000000) + "ms" );
//#endif
        if (halfLoopTime.time < this.minSendInterval.time)
        {
            this.minSendInterval.time -= halfLoopTime.time;
            this.maxSendInterval.time -= halfLoopTime.time;
        }
    }

    public void run() {
//#ifdef PB_DEBUG
        System.out.println("PB: Run-Method of PlanBase started. " );
//#endif
        while (this.running) {
            AlicaTime beginTime = alicaClock.now();
            this.log.itertionStarts();

            if (ae.getStepEngine()) {
//#ifdef PB_DEBUG
                System.out.println("PB: ===CUR TREE===");

                if (this.rootNode == null) {
                    System.out.println( "PB: NULL" );
                }
				else {
                    rootNode.printRecursive();
                }
                System.out.println( "PB: ===END CUR TREE===" );
//#endif
                {
                    // TODO fix implementation
//                    unique_lock<mutex> lckStep(stepMutex);
//                    stepModeCV.wait(
//                    {
//                        return this.ae.getStepCalled();
//                    }
//                    );

                    this.ae.setStepCalled(false);
                    if (!running)
                        return;
                }
                beginTime = alicaClock.now();

            }


            //Send tick to other modules

            this.ae.getCommunicator().tick();


            this.teamObserver.tick(this.rootNode);


            this.ra.tick();

            this.syncModel.tick();

            this.authModul.tick(this.rootNode);

            if (this.rootNode == null) {
                this.rootNode = ruleBook.initialisationRule(this.masterPlan);
            }
            if (this.rootNode.tick(this.ruleBook) == PlanChange.FailChange) {
                System.out.println("PB: MasterPlan Failed");
            }
            //lock for fpEvents
            {
//                lock_guard<mutex> lock(lomutex);
                this.fpEvents = new PriorityQueue<RunningPlan>();
            }

            AlicaTime now = alicaClock.now();

            if (now.time < this.lastSendTime.time)
            {
                // Taker fix
                System.out.println("PB: lastSendTime is in the future of the current system time, did the system time change?" );
                this.lastSendTime.time = now.time;
            }

            if ((this.ruleBook.isChangeOccured() && (this.lastSendTime.time + this.minSendInterval.time) < now.time)
					|| (this.lastSendTime.time + this.maxSendInterval.time) < now.time)
            {
                ArrayList<Long> msg = null;
                this.deepestNode = this.rootNode;
                this.treeDepth = 0;
                this.rootNode.toMessage(msg, this.deepestNode, this.treeDepth, 0);
                this.teamObserver.doBroadCast(msg);
                this.lastSendTime = now;
                this.ruleBook.setChangeOccured(false);
            }

            if (this.sendStatusMessages && (this.lastSentStatusTime.time + this.sendStatusInterval.time) < alicaClock.now().time)
            {
                if (this.deepestNode != null)
                {
                    this.statusMessage.robotIDsWithMe.clear();
                    this.statusMessage.currentPlan = this.deepestNode.getPlan().getName();
                    if (this.deepestNode.getOwnEntryPoint() != null)
                    {
                        this.statusMessage.currentTask = this.deepestNode.getOwnEntryPoint().getTask().getName();
                    }
					else
                    {
                        this.statusMessage.currentTask = "IDLE";
                    }

                    if (this.deepestNode.getActiveState() != null)
                    {
                        this.statusMessage.currentState = this.deepestNode.getActiveState().getName();
                        CommonUtils.copy(this.deepestNode.getAssignment().getRobotStateMapping().getRobotsInState(
                            this.deepestNode.getActiveState()), 0,
                            this.deepestNode.getAssignment().getRobotStateMapping().getRobotsInState(
                                this.deepestNode.getActiveState()
//                            ).size()-1, back_inserter(this.statusMessage.robotIDsWithMe)
                            ).size()-1, (this.statusMessage.robotIDsWithMe)
                        );

                    }
					else
                    {
                        this.statusMessage.currentState = "NONE";
                    }
                    this.statusMessage.currentRole = this.ra.getOwnRole().getName();
                    ae.getCommunicator().sendAlicaEngineInfo(this.statusMessage);
                    this.lastSentStatusTime = alicaClock.now();
                }
            }

            this.log.iterationEnds(this.rootNode);

            this.ae.iterationComplete();

            long availTime;

            now = alicaClock.now();
            if (this.loopTime.time > (now.time - beginTime.time))
            {
                availTime = (long)((this.loopTime.time - (now.time - beginTime.time)) / 1000);
            }
			else
            {
                availTime = 0;
            }

            if (fpEvents.size() > 0)
            {
                //lock for fpEvents
                {
//                    lock_guard<mutex> lock(lomutex);
                    while (this.running && availTime > 1000 && fpEvents.size() > 0)
                    {
                        RunningPlan rp = fpEvents.peek();//front();
                        fpEvents.poll();

                        if (rp.isActive())
                        {
                            boolean first = true;
                            while (rp != null)
                            {
                                System.out.println( "TICK FPEVENT " );
                                PlanChange change = this.ruleBook.visit(rp);
                                System.out.println("AFTER TICK FPEVENT " );
                                if (!first && change == PlanChange.NoChange)
                                {
                                    break;
                                }
                                rp = rp.getParent();
                                first = false;
                            }
                        }
                        now = alicaClock.now();
                        if (this.loopTime.time > (now.time - beginTime.time))
                        {
                            availTime = (long)((this.loopTime.time - (now.time - beginTime.time)) / 1000);
                        }
						else
                        {
                            availTime = 0;
                        }
                    }
                }

            }

//#ifdef PB_DEBUG
            System.out.println("PB: availTime " + availTime );
//#endif
            if (availTime > 1 && !ae.getStepEngine())
            {
                alicaClock.sleep(availTime);
            }
        }
    }

    public void start() {
        if (!this.running)
        {
            this.running = true;
            this.mainThread = new Thread();
            this.mainThread.start();
        }
    }
}
