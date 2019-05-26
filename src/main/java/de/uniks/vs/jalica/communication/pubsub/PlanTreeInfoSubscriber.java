package de.uniks.vs.jalica.communication.pubsub;

import de.uniks.vs.jalica.communication.AlicaZMQCommunication;
import de.uniks.vs.jalica.communication.messages.PlanTreeInfo;
import de.uniks.vs.jalica.unknown.CommonUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;
import org.zeromq.ZMQ;
import org.zeromq.ZMQException;

public class PlanTreeInfoSubscriber extends ZMQSubscriber {

    private final AlicaZMQCommunication alicaZMQCommunication;

    public PlanTreeInfoSubscriber(String topic, ZMQ.Socket subscriber, AlicaZMQCommunication alicaZMQCommunication) {
        super(topic, subscriber);
        this.alicaZMQCommunication = alicaZMQCommunication;

        Thread thread = new Thread() {

            @Override
            public void run() {
                if (CommonUtils.COMM_debug) System.out.println("PTI-Sub("+alicaZMQCommunication.getAe().getAgentName()+"): Thread started" );
                while (true) {

                    try {
                    String string = subscriber.recvStr(0).trim();

                    if (!string.startsWith(topic))
                        continue;

                    if (CommonUtils.COMM_debug) System.out.println("PTI-Sub(" + alicaZMQCommunication.getAe().getAgentName() +") " + string);


                        JSONObject jsonObject = (JSONObject)JSONValue.parseWithException(string.replace(topic, ""));
                        PlanTreeInfo pti = new PlanTreeInfo();
                        pti.senderID = (Long) jsonObject.get("senderID");

                        for ( Object i : (JSONArray) jsonObject.get("stateIDs")) {
                            pti.stateIDs.add((Long) i);
                        }

                        for (Object i : (JSONArray) jsonObject.get("succeededEPs")) {
                            pti.succeededEPs.add((Long) i);
                        }
//                        if (CommonUtils.COMM_debug) System.out.println("PTI-Sub: pti" + pti);
                        alicaZMQCommunication.handlePlanTreeInfo(pti);
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
