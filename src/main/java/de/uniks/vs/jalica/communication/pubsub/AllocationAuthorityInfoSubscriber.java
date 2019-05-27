package de.uniks.vs.jalica.communication.pubsub;

import de.uniks.vs.jalica.communication.AlicaZMQCommunication;
import de.uniks.vs.jalica.engine.common.CommonUtils;
import de.uniks.vs.jalica.engine.containers.messages.AllocationAuthorityInfo;
import de.uniks.vs.jalica.engine.containers.EntryPointAgents;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;
import org.zeromq.ZMQ;
import org.zeromq.ZMQException;

public class AllocationAuthorityInfoSubscriber extends ZMQSubscriber {

    private final AlicaZMQCommunication alicaZMQCommunication;

    public AllocationAuthorityInfoSubscriber(String topic, ZMQ.Socket subscriber, AlicaZMQCommunication alicaZMQCommunication) {
        super(topic, subscriber);
        this.alicaZMQCommunication = alicaZMQCommunication;

        System.out.println("AAI-Sub("+this.alicaZMQCommunication.getAe().getAgentName()+"): start");

        Thread thread = new Thread() {

            @Override
            public void run() {
                    while (true) {

                        try {
                        String string = subscriber.recvStr(0).trim();

                        if (!string.startsWith(topic)) {
                            continue;
                        }

                        if (CommonUtils.COMM_debug) System.out.println("AAI-Sub("+alicaZMQCommunication.getAe().getAgentName()+"): " +string);


                            JSONObject jsonObject = (JSONObject) JSONValue.parseWithException(string.replace(topic, ""));
                            AllocationAuthorityInfo allocationAuthorityInfo = new AllocationAuthorityInfo();

                            allocationAuthorityInfo.senderID = (Long) jsonObject.get("senderID");
                            allocationAuthorityInfo.planType = (Long) jsonObject.get("planType");
                            allocationAuthorityInfo.authority = (Long) jsonObject.get("planType");
                            allocationAuthorityInfo.planID = (Long) jsonObject.get("planID");
                            allocationAuthorityInfo.parentState = (Long) jsonObject.get("parentState");

                            for (Object i : (JSONArray) jsonObject.get("entrypoints")) {
                                JSONObject obj = (JSONObject) i;
                                EntryPointAgents entryPointAgents = new EntryPointAgents();
                                entryPointAgents.entrypoint = (long) obj.get("entryPoint");

                                for (Object _i : (JSONArray) obj.get("robots")) {
                                    entryPointAgents.agents.add((long) _i);
                                }
                                allocationAuthorityInfo.entryPointAgents.add(entryPointAgents);
                            }

//                        for (Object i : (JSONArray) jsonObject.get("succeededEPs")) {
//                            allocationAuthorityInfo.succeededEPs.add((Long) i);
//                        }

                            alicaZMQCommunication.handleAllocationAuthority(allocationAuthorityInfo);

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
