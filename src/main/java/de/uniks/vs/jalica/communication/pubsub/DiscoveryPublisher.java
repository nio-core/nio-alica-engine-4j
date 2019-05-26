package de.uniks.vs.jalica.communication.pubsub;

import de.uniks.vs.jalica.communication.messages.DiscoveryInfo;
import de.uniks.vs.jalica.communication.pubsub.ZMQPublisher;
import de.uniks.vs.jalica.unknown.Message;
import org.json.simple.JSONObject;
import org.zeromq.ZMQ;

public class DiscoveryPublisher extends ZMQPublisher {

    public DiscoveryPublisher(String topic, ZMQ.Socket publisher) {
        super(topic, publisher);
    }

    @Override
    public void publish(Message message) {
        DiscoveryInfo discoveryInfo = (DiscoveryInfo) message;

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("senderID", discoveryInfo.senderID);

        System.out.println("DI-Pub: publish " + jsonObject.toJSONString());
        publisher.send(topic + " " + jsonObject.toJSONString(), 0);
    }
}
