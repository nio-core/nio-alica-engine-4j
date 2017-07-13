package de.uniks.vs.jalica.unknown;

//TODO: mutex check
import com.sun.corba.se.impl.orbutil.concurrent.Mutex;

import de.uniks.vs.jalica.common.Logger;
import de.uniks.vs.jalica.engine.AlicaEngine;
import de.uniks.vs.jalica.supplementary.SystemConfig;
import de.uniks.vs.jalica.teamobserver.ITeamObserver;

/**
 * Created by alex on 13.07.17.
 */
public class PlanBase {
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
        this.loopTime = (AlicaTime)fmax(1000000, lround(1.0 / freq * 1000000000));
        if (this.loopTime.time == 1000000)
        {
            System.out.println("PB: ALICA should not be used with more than 1000Hz . 1000Hz assumed");
        }

        if (minbcfreq > maxbcfreq)
        {
            ae.abort( "PB: Alica.conf: Minimal broadcast frequency must be lower or equal to maximal broadcast frequency!");
        }

        this.minSendInterval = new AlicaTime(fmax(1000000, lround(1.0 / maxbcfreq * 1000000000)));
        this.maxSendInterval = new AlicaTime(fmax(1000000, lround(1.0 / minbcfreq * 1000000000)));

        AlicaTime halfLoopTime = new AlicaTime(this.loopTime.time / 2);
        this.running = false;

        this.sendStatusMessages = Boolean.valueOf(sc.get("Alica").get("Alica.StatusMessages.Enabled"));

        if (sendStatusMessages)
        {
            double stfreq = Double.valueOf(sc.get("Alica").get("Alica.StatusMessages.Frequency"));
            this.sendStatusInterval = new AlicaTime(max(1000000.0, round(1.0 / stfreq * 1000000000)));
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
        System.out.println("PB: Engine loop time is " +(loopTime / 1000000)+ "ms, broadcast interval is "+(this.minSendInterval / 1000000)
                + "ms - " + (this.maxSendInterval / 1000000) + "ms" + );
//#endif
        if (halfLoopTime.time < this.minSendInterval.time)
        {
            this.minSendInterval.time -= halfLoopTime.time;
            this.maxSendInterval.time -= halfLoopTime.time;
        }
    }

    public void run() {

    }

    public void start() {

    }
}
