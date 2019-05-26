package de.uniks.vs.jalica.discovery;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class SenderThread extends Thread {
    private String MULTICAST_IP;
    private int MULTICAST_PORT;

    public SenderThread(String multicastIp, int multicastPort) {

        this.MULTICAST_IP = multicastIp;
        this.MULTICAST_PORT = multicastPort;
    }

    @Override
    public void run() {
        while (!isInterrupted())
            send("SenderThread: hi");
    }

    private void send(String message) {
        try {
            MulticastSocket socket = new MulticastSocket();
            socket.setTimeToLive(1);
            byte[] buf = message.getBytes();
            DatagramPacket pack = new DatagramPacket(buf, buf.length, InetAddress.getByName(MULTICAST_IP), MULTICAST_PORT);
//            DatagramPacket pack = new DatagramPacket(buf, buf.length, InetAddress.getByName("225.4.5.6"), 5000);
            socket.send(pack);
            socket.close();
            sleep(5000);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            // we don't care about sleep() being interrupted
        }
    }
}