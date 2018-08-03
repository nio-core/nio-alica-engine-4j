package de.uniks.vs.jalica.unknown;

import de.uniks.vs.jalica.dummy_proxy.AlicaZMQCommunication;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;
import org.zeromq.ZMQ;

public class AllocationAuthorityInfoSubscriber extends ZMQSubscriber {

    private final AlicaZMQCommunication alicaZMQCommunication;

    public AllocationAuthorityInfoSubscriber(String topic, ZMQ.Socket subscriber, AlicaZMQCommunication alicaZMQCommunication) {
        super(topic, subscriber);
        this.alicaZMQCommunication = alicaZMQCommunication;

        System.out.println("AAI-S: start");

        Thread thread = new Thread() {

            @Override
            public void run() {
                while (true) {

                    String string = subscriber.recvStr(0).trim();

                    if (!string.startsWith(topic))
                        continue;

                    System.out.println("AAI-S: " +string);

                    try {
                        JSONObject jsonObject = (JSONObject) JSONValue.parseWithException(string.replace(topic, ""));
                        AllocationAuthorityInfo ptiPtr = new AllocationAuthorityInfo();

//                        ptiPtr.senderID = (Long) jsonObject.get("senderID");
//
//                        for ( Object i : (JSONArray) jsonObject.get("stateIDs")) {
//                            ptiPtr.stateIDs.add((Long) i);
//                        }
//
//                        for (Object i : (JSONArray) jsonObject.get("succeededEPs")) {
//                            ptiPtr.succeededEPs.add((Long) i);
//                        }

                        alicaZMQCommunication.handleAllocationAuthorityRos(ptiPtr);

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        thread.start();
    }
}
