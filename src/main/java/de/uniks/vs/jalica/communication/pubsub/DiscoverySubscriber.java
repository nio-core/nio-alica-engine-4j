package de.uniks.vs.jalica.communication.pubsub;

import de.uniks.vs.jalica.communication.AlicaZMQCommunication;
import de.uniks.vs.jalica.communication.pubsub.ZMQSubscriber;
import de.uniks.vs.jalica.unknown.CommonUtils;
import de.uniks.vs.jalica.communication.messages.DiscoveryInfo;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;
import org.zeromq.ZMQ;
import org.zeromq.ZMQException;

public class DiscoverySubscriber extends ZMQSubscriber {

    private final AlicaZMQCommunication communication;

    public DiscoverySubscriber(String topic, ZMQ.Socket subscriber, AlicaZMQCommunication communication) {
        super(topic, subscriber);
        this.communication = communication;

        System.out.println("AAI-Sub("+this.communication.getAe().getAgentName()+"): start");

        Thread thread = new Thread() {

            @Override
            public void run() {
                while (true) {

                    try {
                        String string = subscriber.recvStr(0).trim();

                        if (!string.startsWith(topic)) {
                            continue;
                        }

                        if (CommonUtils.COMM_debug) System.out.println("DI-Sub("+communication.getAe().getAgentName()+"): " +string);


                        JSONObject jsonObject = (JSONObject) JSONValue.parseWithException(string.replace(topic, ""));
                        DiscoveryInfo discoveryInfo = new DiscoveryInfo();
                        discoveryInfo.senderID = (String) jsonObject.get("senderID");

                        System.out.println("DI-Sub: info from " + discoveryInfo.senderID);
//                        communication.handleDiscovery(allocationAuthorityInfo);

                    } catch (ParseException e) {
                        e.printStackTrace();
                    } catch (ZMQException e) {
//                            System.err.println(e.getErrorCode());
                    }
                }
            }
        };

        thread.start();
    }
}
