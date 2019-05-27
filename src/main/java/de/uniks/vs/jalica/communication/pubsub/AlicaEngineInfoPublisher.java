package de.uniks.vs.jalica.communication.pubsub;

import de.uniks.vs.jalica.engine.common.CommonUtils;
import de.uniks.vs.jalica.engine.containers.messages.AlicaEngineInfo;
import de.uniks.vs.jalica.engine.containers.Message;
import org.json.simple.JSONObject;
import org.zeromq.ZMQ;

public class AlicaEngineInfoPublisher extends ZMQPublisher {

    public AlicaEngineInfoPublisher(String topic, ZMQ.Socket publisher) {
        super(topic, publisher);
    }

    @Override
    public void publish(Message message) {
        AlicaEngineInfo statInfo = (AlicaEngineInfo) message;

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("currentPlan", statInfo.currentPlan);
        jsonObject.put("currentRole", statInfo.currentRole);
        jsonObject.put("currentState", statInfo.currentState);
        jsonObject.put("currentTask", statInfo.currentTask);
        jsonObject.put("masterPlan", statInfo.masterPlan);
        jsonObject.put("senderID", statInfo.senderID);
        jsonObject.put("agentIDsWithMe", statInfo.agentIDsWithMe.toArray());
        if (CommonUtils.COMM_debug) System.out.println("AEI-Pub("+statInfo.senderID+"): publish " + jsonObject.toJSONString());
        publisher.send(topic + " " + jsonObject.toJSONString(), 0);
    }
}
