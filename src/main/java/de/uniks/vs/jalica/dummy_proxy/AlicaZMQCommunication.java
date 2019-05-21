package de.uniks.vs.jalica.dummy_proxy;

import de.uniks.vs.jalica.engine.AlicaEngine;
import de.uniks.vs.jalica.unknown.*;
import de.uniks.vs.jalica.unknown.Communication.AlicaEngineInfo;
import de.uniks.vs.jalica.unknown.Communication.AllocationAuthorityInfo;
import de.uniks.vs.jalica.unknown.Communication.PlanTreeInfo;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.util.ArrayList;

/**
 * Created by alex on 29.06.18.
 */
public class AlicaZMQCommunication extends AlicaCommunication {

    private String configFile;
    //    private  String alicaEngineInfoTopic;
//    private  String allocationAuthorityInfoTopic;
//    private  String ownRoleTopic;
//    private  String planTreeInfoTopic;
//    private  String syncReadyTopic;
//    private  String syncTalkTopic;
//    private  String solverResultTopic;
    private boolean isRunning;

    private AlicaEngineInfoPublisher alicaEngineInfoPublisher;

    private PlanTreeInfoPublisher planTreeInfoPublisher;
    private PlanTreeInfoSubscriber planTreeInfoSubscriber;

    private AllocationAuthorityInfoPublisher allocationAuthorityInfoPublisher;
    private AllocationAuthorityInfoSubscriber allocationAuthorityInfoSubscriber;

    private SolverResultPublisher solverResultPublisher;
    private SolverResultSubscriber solverResultSubscriber;

    private RoleSwitchPublisher roleSwitchPublisher;

    public AlicaZMQCommunication(AlicaEngine ae) {
        super(ae);
        this.configFile = "AlicaRosProxy";
        this.isRunning = false;
    }

    public AlicaZMQCommunication(AlicaEngine ae, String configFile) {
        super(ae);
        this.configFile = configFile;
        this.isRunning = false;
    }

    public boolean init(ArrayList<Long> ids) {
        //        this.allocationAuthorityInfoTopic = (String) this.systemConfig.get("AlicaRosProxy").get("Topics.allocationAuthorityInfoTopic");
//        this.ownRoleTopic = (String) this.systemConfig.get("AlicaRosProxy").get("Topics.ownRoleTopic");
//        this.alicaEngineInfoTopic = (String) ae.getSystemConfig().get("AlicaRosProxy").get("Topics.alicaEngineInfoTopic");
//        this.planTreeInfoTopic = (String) this.systemConfig.get("AlicaRosProxy").get("Topics.planTreeInfoTopic");
//        this.syncReadyTopic = (String) this.systemConfig.get("AlicaRosProxy").get("Topics.syncReadyTopic");
//        this.syncTalkTopic = (String) this.systemConfig.get("AlicaRosProxy").get("Topics.syncTalkTopic");
//        this.solverResultTopic = (String) this.systemConfig.get("AlicaRosProxy").get("Topics.solverResultTopic");

        ZContext context = new ZContext();
        ZMQ.Socket subscriber = context.createSocket(SocketType.SUB);
//        subscriber.connect("tcp://localhost:5556");
//        if (ae.getSystemConfig().getOwnAgentID() == 1){ //42)
//            subscriber.connect("ipc://" + 2); //17);
//        }
//        else
//            subscriber.connect("ipc://" +  1); //42);

        for (int i = 1; i < 8 ; i++) {
            if (ae.getSystemConfig().getOwnAgentID() != i) {
                subscriber.connect("ipc://" + i);
            }
        }

        ZMQ.Socket publisher = context.createSocket(SocketType.PUB);
//        publisher.bind("tcp://*:5556");
        publisher.bind("ipc://" +  ae.getSystemConfig().getOwnAgentID());

        System.out.println("ZMQ-C: AGENT CHANNEL "+ "ipc://" +  ae.getSystemConfig().getOwnAgentID());

//        AllocationAuthorityInfoPublisher = rosNode.advertise<alica_ros_proxy::AllocationAuthorityInfo>(this.allocationAuthorityInfoTopic, 2);
//        AllocationAuthorityInfoSubscriber = rosNode.subscribe(this.allocationAuthorityInfoTopic, 10, &AlicaRosCommunication::handleAllocationAuthority, (AlicaRosCommunication*)this);
        String allocationAuthorityInfoTopic = (String) ae.getSystemConfig().get(configFile).get("Topics.allocationAuthorityInfoTopic");
        allocationAuthorityInfoPublisher = new AllocationAuthorityInfoPublisher(allocationAuthorityInfoTopic, publisher);
        allocationAuthorityInfoSubscriber = new AllocationAuthorityInfoSubscriber(allocationAuthorityInfoTopic, subscriber, this);


//        AlicaEngineInfoPublisher = rosNode.advertise<alica_ros_proxy::AlicaEngineInfo>(this.alicaEngineInfoTopic, 2);
        String alicaEngineInfoTopic = (String) ae.getSystemConfig().get(configFile).get("Topics.alicaEngineInfoTopic");
        alicaEngineInfoPublisher = new AlicaEngineInfoPublisher(alicaEngineInfoTopic, publisher);
        subscriber.subscribe(alicaEngineInfoTopic.getBytes(ZMQ.CHARSET));

//        RoleSwitchPublisher = rosNode.advertise<alica_ros_proxy::RoleSwitch>(this.ownRoleTopic,  10);
        String ownRoleTopic = (String) ae.getSystemConfig().get(configFile).get("Topics.ownRoleTopic");
        roleSwitchPublisher = new RoleSwitchPublisher(ownRoleTopic, publisher);
        subscriber.subscribe(ownRoleTopic.getBytes(ZMQ.CHARSET));


//        PlanTreeInfoPublisher = rosNode.advertise<alica_ros_proxy::PlanTreeInfo>(this.planTreeInfoTopic, 10);
//        PlanTreeInfoSubscriber = ro sNode.subscribe(this.planTreeInfoTopic, 1, &AlicaRosCommunication::handlePlanTreeInfo,  (AlicaRosCommunication*)this);
        String planTreeInfoTopic = (String) ae.getSystemConfig().get(configFile).get("Topics.planTreeInfoTopic");
        planTreeInfoPublisher = new PlanTreeInfoPublisher(planTreeInfoTopic, publisher);
        planTreeInfoSubscriber = new PlanTreeInfoSubscriber(planTreeInfoTopic, subscriber, this);
//
//        SyncReadyPublisher = rosNode.advertise<alica_ros_proxy::SyncReady>(this.syncReadyTopic, 10);
//        SyncReadySubscriber = rosNode.subscribe(this.syncReadyTopic, 5, &AlicaRosCommunication::handleSyncReadyRos, (AlicaRosCommunication*)this);

//        SyncTalkPublisher = rosNode.advertise<alica_ros_proxy::SyncTalk>(this.syncTalkTopic, 10);
//        SyncTalkSubscriber = rosNode.subscribe(this.syncTalkTopic, 5, &AlicaRosCommunication::handleSyncTalkRos, (AlicaRosCommunication*)this);
//
//        SolverResultPublisher = rosNode.advertise<alica_ros_proxy::SolverResult>(this.solverResultTopic, 10);
//        SolverResultSubscriber = rosNode.subscribe(this.solverResultTopic, 5, &AlicaRosCommunication::handleSolverResult, (AlicaRosCommunication*)this);
        String solverResultTopic = (String) ae.getSystemConfig().get(configFile).get("Topics.solverResultTopic");
        solverResultPublisher = new SolverResultPublisher(solverResultTopic, publisher);
        solverResultSubscriber = new SolverResultSubscriber(solverResultTopic, subscriber, this);

        return true;
    }

    @Override
    public void startCommunication() {
        this.isRunning = true;
    }

    @Override
    public void stopCommunication() {
        this.isRunning = false;
    }

    @Override
    public void tick() {

        if (this.isRunning) {
            //Use this for synchronous communication!
        }
    }


    //TODO: chain of responsibility (all publisher)
    @Override
    public void sendAllocationAuthority(AllocationAuthorityInfo aai) {

        if (this.isRunning){
            this.allocationAuthorityInfoPublisher.publish(aai);
        }
    }

    @Override
    public void sendAlicaEngineInfo(AlicaEngineInfo statInfo) {

        if (this.isRunning) {
            this.alicaEngineInfoPublisher.publish(statInfo);
        }
    }

    @Override
    public void sendRoleSwitch(RoleSwitch rs) {

        if (this.isRunning){
            this.roleSwitchPublisher.publish(rs);
        }
    }

    @Override
    public void sendPlanTreeInfo(PlanTreeInfo planTreeInfo) {

        if (this.isRunning) {
            planTreeInfoPublisher.publish(planTreeInfo);
        }
    }

    @Override
    public void sendSyncReady(SyncReady sr) {
        CommonUtils.aboutNoImpl();
        if (this.isRunning){
//            this.SyncReadyPublisher.publish(sr);
        }
    }

    @Override
    public void sendSyncTalk(SyncTalk st) {
        CommonUtils.aboutNoImpl();
        if (this.isRunning){
//            this.SyncTalkPublisher.publish(st);
        }
    }

    @Override
    public void sendSolverResult(SolverResult sr) {

        if (this.isRunning) {
            this.solverResultPublisher.publish(sr);
        }
    }



    public void handleAllocationAuthority(AllocationAuthorityInfo aai) {

        if (this.isRunning) {
            this.onAuthorityInfoReceived(aai);
        }
    }

    public void handlePlanTreeInfo(PlanTreeInfo pti) {

        if (this.isRunning) {
            this.onPlanTreeInfoReceived(pti);
        }
    }

//    public void handleSyncReadyRos(SyncReady sr) {
//
//        if (this.isRunning) {
//            this.onSyncReadyReceived(sr);
//        }
//    }

//    public void handleSyncTalkRos(SyncTalk st) {
//        if (this.isRunning)
//        {
//            this.onSyncTalkReceived(st);
//        }
//    }

    public void handleSolverResult(SolverResult sr) {

        if (this.isRunning) {
            this.onSolverResult(sr);
        }
    }
}
