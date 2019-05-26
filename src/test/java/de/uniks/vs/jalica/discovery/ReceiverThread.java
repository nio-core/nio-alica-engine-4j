package de.uniks.vs.jalica.discovery;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.util.*;
import java.util.Map.Entry;

public class ReceiverThread extends Thread {
    private Map<String, Long> hosts = new HashMap<String, Long>();
    private String MULTICAST_INTERFACE;
    private String MULTICAST_IP;
    private final int MULTICAST_PORT;

    public ReceiverThread(String multicastInterface, String multicastIp, int port) {
        this.MULTICAST_INTERFACE = multicastInterface;
        this.MULTICAST_IP = multicastIp;
        this.MULTICAST_PORT = port;
    }

    @Override
    public void run() {
        try {
            MulticastSocket socket = new MulticastSocket(MULTICAST_PORT);
//            MulticastSocket socket = new MulticastSocket(5000);
            NetworkInterface networkInterface = NetworkInterface.getByName(MULTICAST_INTERFACE);

//            NetworkInterface networkInterface = NetworkInterface.getByIndex(1);
            Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
            InetAddress inetAddress = inetAddresses.nextElement();
            socket.setInterface(inetAddress);
            socket.setNetworkInterface(networkInterface);
            socket.setLoopbackMode(true);
            socket.setReuseAddress(true);
            socket.joinGroup(InetAddress.getByName(MULTICAST_IP));
//            socket.joinGroup(InetAddress.getByName("225.4.5.6"));

            while (!isInterrupted()) {
                byte buf[] = new byte[1024];
                DatagramPacket pack = new DatagramPacket(buf, buf.length);
                socket.receive(pack);

                String message = new String(pack.getData(), 0, pack.getLength());

                if (message.equals("hi")) {
                    hosts.put(pack.getAddress().getHostAddress(), System.currentTimeMillis());
                }
                System.out.println(getAvailableHosts() + " " + message);
            }

            socket.leaveGroup(InetAddress.getByName(MULTICAST_IP));
//            socket.leaveGroup(InetAddress.getByName("225.4.5.6"));
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Set<String> getAvailableHosts() {
        List<String> hostsToRemove = new ArrayList<String>();
        for (Entry<String, Long> entry : hosts.entrySet()) {
            long entryAge = System.currentTimeMillis() - entry.getValue();
            if (entryAge > 10000) {
                hostsToRemove.add(entry.getKey());
            }
        }

        for (String host : hostsToRemove) hosts.remove(host);
        return hosts.keySet();
    }
}