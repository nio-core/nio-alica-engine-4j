package de.uniks.vs.jalica.communication;

import de.uniks.vs.jalica.common.utils.CommonUtils;
import de.uniks.vs.jalica.communication.discovery.StdDiscovery;
import de.uniks.vs.jalica.communication.pubsub.*;
import de.uniks.vs.jalica.engine.IAlicaCommunication;
import de.uniks.vs.jalica.engine.AlicaEngine;
import de.uniks.vs.jalica.engine.containers.messages.AlicaEngineInfo;
import de.uniks.vs.jalica.engine.containers.messages.AllocationAuthorityInfo;
import de.uniks.vs.jalica.engine.containers.messages.PlanTreeInfo;
import de.uniks.vs.jalica.engine.containers.RoleSwitch;
import de.uniks.vs.jalica.engine.containers.SolverResult;
import de.uniks.vs.jalica.engine.containers.SyncReady;
import de.uniks.vs.jalica.engine.containers.SyncTalk;
import org.zeromq.*;

/**
 * Created by alex on 29.06.18.
 */
public class AlicaZMQCommunication extends IAlicaCommunication {

    private final MessageTopics topics;

    private AlicaEngineInfoPublisher alicaEngineInfoPublisher;

    private PlanTreeInfoPublisher planTreeInfoPublisher;
    private AllocationAuthorityInfoPublisher allocationAuthorityInfoPublisher;
    private SolverResultPublisher solverResultPublisher;
    private RoleSwitchPublisher roleSwitchPublisher;
    private boolean isRunning;

    private StdDiscovery discovery;

//    private DiscoveryPublisher discoveryPublisher;
//    private DiscoverySubscriber discoverySubscriber;

    public AlicaZMQCommunication(AlicaEngine ae) {
        this(ae, "AlicaCommuicationTopics");
    }

    public AlicaZMQCommunication(AlicaEngine ae, String configFile) {
        super(ae);
        this.topics = new MessageTopics(ae, configFile);
        this.isRunning = false;
        System.setProperty("java.net.preferIPv4Stack", "true");
    }

//    public boolean init(ArrayList<Long> ids) {
    public boolean init() {
        topics.loadTopics();
        ZContext context = new ZContext();
        initializeMsgPublishers(context);
        initDiscovery(context);
//        ZMQ.Socket subscriber = context.createSocket(SocketType.SUB);

//        for (int i = 0; i < 7; i++)
//            if (i != ae.getSystemConfig().getOwnAgentID()) {
//                initMsgSubscriber(context, i + "", topics, subscriber);
//                try {
//                    Thread.sleep(10);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
        return true;
    }

//    private void initMsgSubscriber(ZContext context, String id, Topics topics, ZMQ.Socket subscriber) {
//        commNodes.add(new CommunicationNode(context, id, topics, this, subscriber));
//    }

    private void initDiscovery(ZContext context) {
        ZMQ.Socket subscriber = context.createSocket(SocketType.SUB);
        discovery = new StdDiscovery(this, context, subscriber, topics);
    }

    private void initializeMsgPublishers(ZContext context) {
        ZMQ.Socket publisher = context.createSocket(SocketType.PUB);
        publisher.bind("ipc://" +  ae.getSystemConfig().getOwnAgentID());
//        publisher.bind("tcp://*:5556");
        System.out.println("ZMQ-C: AGENT CHANNEL "+ "ipc://" +  ae.getSystemConfig().getOwnAgentID());
        allocationAuthorityInfoPublisher = new AllocationAuthorityInfoPublisher(topics.getTopic(MessageTopics.Type.allocationAuthorityInfoTopic), publisher);
        alicaEngineInfoPublisher = new AlicaEngineInfoPublisher(topics.getTopic(MessageTopics.Type.alicaEngineInfoTopic), publisher);
        planTreeInfoPublisher = new PlanTreeInfoPublisher(topics.getTopic(MessageTopics.Type.planTreeInfoTopic), publisher);
//        roleSwitchPublisher = new RoleSwitchPublisher(topics.getTopic(Topics.Type.ownRoleTopic), publisher);
//        SyncReadyPublisher = rosNode.advertise<alica_ros_proxy::SyncReady>(this.syncReadyTopic, 10);
//        SyncTalkPublisher = rosNode.advertise<alica_ros_proxy::SyncTalk>(this.syncTalkTopic, 10);
//        solverResultPublisher = new SolverResultPublisher(topics.getTopic(Topics.Type.solverResultTopic), publisher);
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

//    public void handleSyncReady(SyncReady sr) {
//
//        if (this.isRunning) {
//            this.onSyncReadyReceived(sr);
//        }
//    }

//    public void handleSyncTalk(SyncTalk st) {
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
