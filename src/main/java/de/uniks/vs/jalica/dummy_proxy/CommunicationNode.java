package de.uniks.vs.jalica.dummy_proxy;

import de.uniks.vs.jalica.unknown.AllocationAuthorityInfoSubscriber;
import de.uniks.vs.jalica.unknown.PlanTreeInfoSubscriber;
import de.uniks.vs.jalica.unknown.SolverResultSubscriber;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class CommunicationNode {

    private final ZContext context;
    private final long id;

    private AllocationAuthorityInfoSubscriber allocationAuthorityInfoSubscriber;
    private PlanTreeInfoSubscriber planTreeInfoSubscriber;
    private SolverResultSubscriber solverResultSubscriber;

    private long signOfLife = -1;

    public CommunicationNode(ZContext context, long id, Topics topics, AlicaZMQCommunication communication, ZMQ.Socket subscriber) {
        this.context = context;
        this.id = id;

        subscriber.connect("ipc://" + id);

        subscriber.subscribe(topics.getTopic(Topics.Type.alicaEngineInfoTopic).getBytes(ZMQ.CHARSET));
        subscriber.subscribe(topics.getTopic(Topics.Type.ownRoleTopic).getBytes(ZMQ.CHARSET));

        allocationAuthorityInfoSubscriber = new AllocationAuthorityInfoSubscriber(topics.getTopic(Topics.Type.allocationAuthorityInfoTopic), subscriber, communication);
        planTreeInfoSubscriber = new PlanTreeInfoSubscriber(topics.getTopic(Topics.Type.planTreeInfoTopic), subscriber, communication);
//        SyncReadySubscriber = rosNode.subscribe(this.syncReadyTopic, 5, &AlicaRosCommunication::handleSyncReadyRos, (AlicaRosCommunication*)this);
//        SyncTalkSubscriber = rosNode.subscribe(this.syncTalkTopic, 5, &AlicaRosCommunication::handleSyncTalkRos, (AlicaRosCommunication*)this);
//        solverResultSubscriber = new SolverResultSubscriber(topics.getTopic(Topics.Type.solverResultTopic), subscriber, communication);
    }
}

//        subscriber.connect("tcp://localhost:5556");
//        subscriber.connect("ipc://" + 2); //17);