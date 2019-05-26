package de.uniks.vs.jalica.discovery;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.util.Enumeration;

public class UDPMulticastClient implements Runnable {

    private String MULTICAST_INTERFACE;
    private String MULTICAST_IP;
    private int MULTICAST_PORT;

    public UDPMulticastClient(String multicastInterface, String multicastIp, int multicastPort) {
        this.MULTICAST_INTERFACE = multicastInterface;
        this.MULTICAST_IP = multicastIp;
        this.MULTICAST_PORT = multicastPort;
    }

    public void receiveUDPMessage(String ip, int port) throws IOException {
        byte[] buffer = new byte[1024];
        MulticastSocket multicastSocket = new MulticastSocket(MULTICAST_PORT);
//        MulticastSocket multicastSocket = new MulticastSocket(4321);

        NetworkInterface networkInterface = NetworkInterface.getByName(MULTICAST_INTERFACE);

//        NetworkInterface networkInterface = NetworkInterface.getByIndex(1);
        Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
        InetAddress inetAddress = inetAddresses.nextElement();

        multicastSocket.setReuseAddress(true);
        multicastSocket.setInterface(inetAddress);
        multicastSocket.setNetworkInterface(networkInterface);
        multicastSocket.setLoopbackMode(true);

        InetAddress group = InetAddress.getByName(MULTICAST_IP);
//        InetAddress group = InetAddress.getByName("230.0.0.0");
        multicastSocket.joinGroup(group);

        while (true) {
            System.out.println("Waiting for multicast message...");
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            multicastSocket.receive(packet);
            String msg = new String(packet.getData(), packet.getOffset(), packet.getLength());
            System.out.println("[Multicast UDP message received] " + msg);

            if ("OK".equals(msg)) {
                System.out.println("No more message. Exiting : " + msg);
                break;
            }
        }
        multicastSocket.leaveGroup(group);
        multicastSocket.close();
    }

    @Override
    public void run() {
        try {
            receiveUDPMessage("230.0.0.0", 4321);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}