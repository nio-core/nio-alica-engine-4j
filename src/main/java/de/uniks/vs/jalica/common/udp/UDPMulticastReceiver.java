package de.uniks.vs.jalica.common.udp;

import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.Map.Entry;

public class UDPMulticastReceiver extends Thread {
    private  MulticastSocket socket;
    private Map<String, String> hosts = new HashMap<String, String>();
    private String MULTICAST_INTERFACE;
    private String MULTICAST_IP;
    private int MULTICAST_PORT;
    private boolean socketFailure = false;

    public UDPMulticastReceiver(String multicastInterface, String multicastIp, int port) {
        try {
        this.MULTICAST_INTERFACE = multicastInterface;
        this.MULTICAST_IP = multicastIp;
        this.MULTICAST_PORT = port;
        socket = new MulticastSocket(MULTICAST_PORT);
        NetworkInterface networkInterface = NetworkInterface.getByName(MULTICAST_INTERFACE);
        Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
        InetAddress inetAddress = inetAddresses.nextElement();
        socket.setInterface(inetAddress);
        socket.setNetworkInterface(networkInterface);
        socket.setLoopbackMode(true);
        socket.setReuseAddress(true);
        socket.joinGroup(InetAddress.getByName(MULTICAST_IP));
        } catch (IOException e) {
        socketFailure = true;
    }

    }

    public Map<String, String> getHosts() {
        return hosts;
    }

    @Override
    public void run() {
        try {

            while (!isInterrupted()) {
                byte buf[] = new byte[1024];
                DatagramPacket pack = new DatagramPacket(buf, buf.length);
                socket.receive(pack);
                String message = new String(pack.getData(), 0, pack.getLength());
                hosts.put(pack.getAddress().getHostAddress(), message);
            }
            socket.leaveGroup(InetAddress.getByName(MULTICAST_IP));
            socket.close();
        } catch (IOException e) {
            socketFailure = true;
        }
    }

    public boolean getSocketFailure() {
        return this.socketFailure;
    }
}