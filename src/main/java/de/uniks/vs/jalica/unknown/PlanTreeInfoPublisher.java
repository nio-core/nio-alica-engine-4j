package de.uniks.vs.jalica.unknown;

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
        if (CommonUtils.COMM_debug) System.out.println("PTI-Pub: publish " + jsonObject.toJSONString());
        publisher.send(topic + " " + jsonObject.toJSONString(), 0);
    }
}
