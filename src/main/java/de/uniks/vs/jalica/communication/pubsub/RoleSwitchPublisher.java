package de.uniks.vs.jalica.communication.pubsub;

import de.uniks.vs.jalica.unknown.Message;
import de.uniks.vs.jalica.unknown.RoleSwitch;
import org.json.simple.JSONObject;
import org.zeromq.ZMQ;

public class RoleSwitchPublisher extends ZMQPublisher {

    public RoleSwitchPublisher(String topic, ZMQ.Socket publisher) {
        super(topic, publisher);
    }

    @Override
    public void publish(Message message) {
        RoleSwitch statInfo = (RoleSwitch) message;

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("roleID", statInfo.roleID);
//        jsonObject.put("agentIDsWithMe", statInfo.agentIDsWithMe.toArray());
        System.out.println("RS-Pub: publish " + jsonObject.toJSONString());
        publisher.send(topic + " " + jsonObject.toJSONString(), 0);
    }
}
