package de.uniks.vs.jalica.communication.pubsub;

import de.uniks.vs.jalica.engine.containers.messages.PlanTreeInfo;
import de.uniks.vs.jalica.engine.common.CommonUtils;
import de.uniks.vs.jalica.engine.containers.Message;
import org.json.simple.JSONObject;
import org.zeromq.ZMQ;

public class PlanTreeInfoPublisher extends ZMQPublisher {


    public PlanTreeInfoPublisher(String topic, ZMQ.Socket publisher) {
        super(topic, publisher);
    }

    @Override
    public void publish(Message message) {
        PlanTreeInfo statInfo = (PlanTreeInfo) message;

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("senderID", statInfo.senderID);
        jsonObject.put("stateIDs", statInfo.stateIDs.toArray());
        jsonObject.put("succeededEPs", statInfo.succeededEPs.toArray());
        if (CommonUtils.COMM_debug) System.out.println("PTI-Pub("+statInfo.senderID+"): publish " + jsonObject.toJSONString());
        publisher.send(topic + " " + jsonObject.toJSONString(), 0);
    }
}
