package de.uniks.vs.jalica.unknown;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.zeromq.ZMQ;

public class AllocationAuthorityInfoPublisher extends ZMQPublisher {

    public AllocationAuthorityInfoPublisher(String topic, ZMQ.Socket publisher) {
        super(topic, publisher);
    }

    @Override
    public void publish(Message message) {
        AllocationAuthorityInfo authorityInfo = (AllocationAuthorityInfo) message;

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("planID", authorityInfo.planId);
        jsonObject.put("parentState", authorityInfo.parentState);
        jsonObject.put("planType", authorityInfo.planType);
        jsonObject.put("authority", authorityInfo.authority);

        JSONArray jsonArray = new JSONArray();

        for (EntryPointAgents ep : authorityInfo.entryPointAgents) {
            JSONObject newEP = new JSONObject();
            newEP.put("entryPoint", ep.entrypoint);
            newEP.put("robots", ep.agents.toArray());
            jsonArray.add(newEP);
        }
        jsonObject.put("entrypoints", jsonArray);
        System.out.println("AA-Pub: publish " + jsonObject.toJSONString());
        publisher.send(topic + " " + jsonObject.toJSONString(), 0);
    }
}
