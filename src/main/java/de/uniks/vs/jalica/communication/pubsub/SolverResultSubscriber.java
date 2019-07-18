package de.uniks.vs.jalica.communication.pubsub;

import de.uniks.vs.jalica.communication.AlicaZMQCommunication;
import de.uniks.vs.jalica.common.utils.CommonUtils;
import de.uniks.vs.jalica.engine.containers.SolverResult;
import de.uniks.vs.jalica.engine.containers.SolverVar;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;
import org.zeromq.ZMQ;
import org.zeromq.ZMQException;

import java.util.ArrayList;
import java.util.Vector;

public class SolverResultSubscriber extends ZMQSubscriber {

    private final AlicaZMQCommunication alicaZMQCommunication;

    public SolverResultSubscriber(String topic, ZMQ.Socket subscriber, AlicaZMQCommunication alicaZMQCommunication) {
        super(topic, subscriber);
        this.alicaZMQCommunication = alicaZMQCommunication;

        Thread thread = new Thread() {

            @Override
            public void run() {
                if (CommonUtils.COMM_debug) CommonUtils.aboutCallNotification();
                String string = null;

                while (true) {
                    try {
                        string = subscriber.recvStr(0).trim();

                        if (!string.startsWith(topic))
                            continue;

                        System.out.println("SR-Sub: " + string);
                        JSONObject jsonObject = (JSONObject) JSONValue.parseWithException(string.replace(topic, ""));
                        SolverResult solverResult = new SolverResult();
                        solverResult.senderID = alicaZMQCommunication.getAe().getId((String)jsonObject.get("senderID"));

                        for (Object sv : (JSONArray) jsonObject.get("vars")) {
                            JSONObject object = (JSONObject) sv;
                            SolverVar svs = new SolverVar();
                            svs.id = (long) object.get("id");
                            svs.value = (ArrayList<Integer>) object.get("value");
                            solverResult.vars.add(svs);
                        }
                        alicaZMQCommunication.handleSolverResult(solverResult);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    } catch (ZMQException e) {
//                        System.err.println(e.getErrorCode());
                    }
                }
            }
        };
        thread.start();
    }
}
