package de.uniks.vs.jalica.communication;

import de.uniks.vs.jalica.communication.pubsub.AllocationAuthorityInfoSubscriber;
import de.uniks.vs.jalica.communication.pubsub.PlanTreeInfoSubscriber;
import de.uniks.vs.jalica.communication.pubsub.SolverResultSubscriber;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class NetworkNode {

    private final ZContext context;
    private final long id;

    private AllocationAuthorityInfoSubscriber allocationAuthorityInfoSubscriber;
    private PlanTreeInfoSubscriber planTreeInfoSubscriber;
    private SolverResultSubscriber solverResultSubscriber;

    private long signOfLife = -1;

    public NetworkNode(ZContext context, long id, MessageTopics topics, AlicaZMQCommunication communication, ZMQ.Socket subscriber) {
        this.context = context;
        this.id = id;

        subscriber.connect("ipc://" + id);

        subscriber.subscribe(topics.getTopic(MessageTopics.Type.alicaEngineInfoTopic).getBytes(ZMQ.CHARSET));
        subscriber.subscribe(topics.getTopic(MessageTopics.Type.ownRoleTopic).getBytes(ZMQ.CHARSET));

        allocationAuthorityInfoSubscriber = new AllocationAuthorityInfoSubscriber(topics.getTopic(MessageTopics.Type.allocationAuthorityInfoTopic), subscriber, communication);
        planTreeInfoSubscriber = new PlanTreeInfoSubscriber(topics.getTopic(MessageTopics.Type.planTreeInfoTopic), subscriber, communication);
//        SyncReadySubscriber = rosNode.subscribe(this.syncReadyTopic, 5, &AlicaRosCommunication::handleSyncReadyRos, (AlicaRosCommunication*)this);
//        SyncTalkSubscriber = rosNode.subscribe(this.syncTalkTopic, 5, &AlicaRosCommunication::handleSyncTalkRos, (AlicaRosCommunication*)this);
//        solverResultSubscriber = new SolverResultSubscriber(topics.getTopic(Topics.Type.solverResultTopic), subscriber, communication);
    }
}

//        subscriber.connect("tcp://localhost:5556");
//        subscriber.connect("ipc://" + 2); //17);