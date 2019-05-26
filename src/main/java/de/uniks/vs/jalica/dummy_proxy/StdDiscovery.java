package de.uniks.vs.jalica.dummy_proxy;

import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.util.*;

public class StdDiscovery extends Discovery implements Runnable {

    //    private static final String MULTICAST_INTERFACE = "en7"; //"en0" //"eth0";
    protected static final String MULTICAST_INTERFACE = "en0"; //"en0" //"eth0";
    protected static final int MULTICAST_PORT = 4446;
    protected static final String MULTICAST_IP = "230.0.0.1";

    private Thread thread;
    private MulticastSocket multicastSocket;

    private final AlicaZMQCommunication communication;
    private final ZContext context;
    private final ZMQ.Socket subscriber;
    private final Topics topics;

    private Map<String, Long> hosts = new HashMap<String, Long>();


    public StdDiscovery(AlicaZMQCommunication communication, ZContext context, ZMQ.Socket subscriber, Topics topics) {
        this.ownID = communication.getAe().getSystemConfig().getOwnAgentID();
        this.communication = communication;
        this.context = context;
        this.subscriber = subscriber;
        this.topics = topics;
        init();
    }

    protected void init() {

        try {
            initMulticastListener();
        } catch (IOException e) {
            e.printStackTrace();
        }
        startDiscovery();
    }

    private void initMulticastListener() throws IOException {
        NetworkInterface networkInterface = NetworkInterface.getByIndex(1);
        networkInterface = NetworkInterface.getByName(MULTICAST_INTERFACE);
        Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
        InetAddress inetAddress = inetAddresses.nextElement();

        multicastSocket = new MulticastSocket(MULTICAST_PORT);
        multicastSocket.setInterface(inetAddress);
        multicastSocket.setNetworkInterface(networkInterface);
        multicastSocket.setTimeToLive(0); //20 //[0;255]
        multicastSocket.setLoopbackMode(true);
        multicastSocket.setReuseAddress(true);

        System.out.println("SD: Time to Live : " + multicastSocket.getTimeToLive());
        System.out.println("SD: Interface : " + multicastSocket.getInterface());
        System.out.println("SD: Network Inteface : " + multicastSocket.getNetworkInterface());
        System.out.println("SD: Loopback mode : " + multicastSocket.getLoopbackMode());

        InetAddress group = InetAddress.getByName(MULTICAST_IP);
        multicastSocket.joinGroup(group);
    }

    private void startDiscovery() {
        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        startMulticastListener();

        Random random = new Random();

        while (true) {
            try {
                Thread.sleep(random.nextInt(20)* 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            send(String.valueOf(ownID));

        }
    }

    private void startMulticastListener() {
        Thread listener = new Thread() {
            @Override
            public void run() {

                try {

                    while (!isInterrupted()) {
                        byte buf[] = new byte[1024];
                        DatagramPacket pack = new DatagramPacket(buf, buf.length);
                        multicastSocket.receive(pack);
                        String message = new String(pack.getData(), 0, pack.getLength());
                        hosts.put(pack.getAddress().getHostAddress(), System.currentTimeMillis());
                        Long agentID = Long.valueOf(message);
//                        System.out.println("Agent("+ownID+")  "+getAvailableHosts() + " " + message);

                        if (!commNodes.containsKey(agentID)) {
                            commNodes.put(agentID, new CommunicationNode(context, agentID, topics, communication, subscriber));
                        }
                    }
                    multicastSocket.leaveGroup(InetAddress.getByName(MULTICAST_IP));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        listener.start();
    }

    private void send(String message) {
        try {
            byte[] buf = message.getBytes();
            DatagramPacket pack = new DatagramPacket(buf, buf.length, InetAddress.getByName(MULTICAST_IP), MULTICAST_PORT);
            multicastSocket.send(pack);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Set<String> getAvailableHosts() {
        List<String> hostsToRemove = new ArrayList<String>();

        for (Map.Entry<String, Long> entry : hosts.entrySet()) {
            long entryAge = System.currentTimeMillis() - entry.getValue();
            if (entryAge > 10000) {
                hostsToRemove.add(entry.getKey());
            }
        }

        for (String host : hostsToRemove) hosts.remove(host);
        return hosts.keySet();
    }
}
