package de.uniks.vs.jalica.unknown;

import de.uniks.vs.jalica.dummy_proxy.AlicaZMQCommunication;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;
import org.zeromq.ZMQ;
import org.zeromq.ZMQException;

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
                        solverResult.senderID = (Long) jsonObject.get("senderID");

                        for (Object sv : (JSONArray) jsonObject.get("vars")) {
                            JSONObject object = (JSONObject) sv;
                            SolverVar svs = new SolverVar();
                            svs.id = (long) object.get("id");
                            svs.value = (Vector<Integer>) object.get("value");
                            solverResult.vars.add(svs);
                        }
                        alicaZMQCommunication.handleSolverResult(solverResult);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    } catch (ZMQException e) {
                        System.err.println(e.getErrorCode());
                    }
                }
            }
        };
        thread.start();
    }
}
