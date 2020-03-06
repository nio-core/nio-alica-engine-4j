package de.uniks.vs.jalica.communication.pubsub;

import de.uniks.vs.jalica.common.utils.CommonUtils;
import de.uniks.vs.jalica.engine.containers.Message;
import de.uniks.vs.jalica.engine.containers.SolverResult;
import de.uniks.vs.jalica.engine.containers.SolverVar;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.zeromq.ZMQ;

public class SolverResultPublisher extends ZMQPublisher {


    public SolverResultPublisher(String topic, ZMQ.Socket publisher) {
        super(topic, publisher);
    }

    @Override
    public void publish(Message message) {
        SolverResult solverResult = (SolverResult) message;

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("senderID", solverResult.senderID);

        JSONArray jsonArray = new JSONArray();

        for (SolverVar solverVar : solverResult.vars) {
            JSONObject object = new JSONObject();
            object.put("id", solverVar.id);
            object.put("value", solverVar.value);
            jsonArray.add(object);
        }

        jsonObject.put("vars", jsonArray);

        if (CommonUtils.COMM_debug) System.out.println("SR-Pub("+solverResult.senderID+"): publish " + jsonObject.toJSONString());
        publisher.send(topic + " " + jsonObject.toJSONString(), 0);
    }
}
