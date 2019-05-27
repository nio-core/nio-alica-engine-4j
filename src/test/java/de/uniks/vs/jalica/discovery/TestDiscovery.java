package de.uniks.vs.jalica.discovery;

import de.uniks.vs.jalica.engine.common.CommonNetworkUtils;
import org.junit.Test;

import java.io.IOException;
import java.net.*;

public class TestDiscovery {


//    private static final String MULTICAST_INTERFACE = "en7"; //"en0" //"eth0";
    private static final String MULTICAST_INTERFACE = "en0";
    private static final int MULTICAST_PORT = 4446;
    private static final String MULTICAST_IP = "230.0.0.1";
//    private static final String MULTICAST_IP = "localhost";//"230.0.0.1";
    private InetSocketAddress socketAddress;


    @Test
    public void testMulticastDiscovery() throws InterruptedException {
//        new StdDiscovery(20);
//        new StdDiscovery(21);
//        new StdDiscovery(22);
//        Thread.sleep(300000);
//        new MulticastPublisher(MULTICAST_IP, MULTICAST_PORT);
//        new MulticastSubscriber(MULTICAST_IP, MULTICAST_PORT);
    }


    @Test
    public void testJavaDatagramChannelMulticast1() throws IOException, InterruptedException {
        System.setProperty("java.net.preferIPv6Stack", "true");
        CommonNetworkUtils.getAvaibleNetworkInterfaces();

        Thread MulticastReceiver = new Thread() {

            @Override
            public void run() {
                DatagramChannelMulticastReceiver mr = new DatagramChannelMulticastReceiver();
                try {
                    System.out.println("Message received : " + mr.receiveMessage(MULTICAST_IP, MULTICAST_PORT, MULTICAST_INTERFACE));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        MulticastReceiver.start();

        Thread.sleep(4000);

        Thread MulticastPublisher = new Thread() {

            @Override
            public void run() {
                DatagramChannelMulticastPublisher mp = new DatagramChannelMulticastPublisher();
                try {
                    mp.sendMessage(MULTICAST_IP, MULTICAST_PORT, MULTICAST_INTERFACE, "Hi there!");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        MulticastPublisher.start();

        Thread.sleep(4000);
    }

    @Test
    public void testJavaMulticast1() throws IOException, InterruptedException {

        Thread t = new Thread(new UDPMulticastClient(MULTICAST_INTERFACE, MULTICAST_IP, MULTICAST_PORT));
        t.start();

        Thread.sleep(2000);

        System.out.println("send");
        UDPMulticastServer.sendUDPMessage("This is a multicast messge", "230.0.0.0", MULTICAST_PORT);
        System.out.println("send");
        UDPMulticastServer.sendUDPMessage("This is the second multicast messge", "230.0.0.0", MULTICAST_PORT);
        System.out.println("send");
        UDPMulticastServer.sendUDPMessage("This is the third multicast messge", "230.0.0.0", MULTICAST_PORT);
        System.out.println("send");
        UDPMulticastServer.sendUDPMessage("OK", "230.0.0.0", MULTICAST_PORT);
        System.out.println("send");

        Thread.sleep(4000);

    }



    @Test
    public void testJavaMulticast2() throws InterruptedException, IOException {
//        System.setProperty("java.net.preferIPv4Stack", "true");

        final ReceiverThread receiverThread = new ReceiverThread(MULTICAST_INTERFACE, MULTICAST_IP, MULTICAST_PORT);
        final SenderThread senderThread = new SenderThread(MULTICAST_IP,MULTICAST_PORT);

        receiverThread.start();
        Thread.sleep(2000);
        senderThread.start();
        UDPMulticastServer.sendUDPMessage("UDPMulticastServer: This is the third multicast message", MULTICAST_IP, MULTICAST_PORT);

        DatagramChannelMulticastPublisher mp = new DatagramChannelMulticastPublisher();
        try {
            mp.sendMessage(MULTICAST_IP, MULTICAST_PORT, MULTICAST_INTERFACE, "DatagramChannelMulticastPublisher: Hi there!");
        } catch (IOException e) {
            e.printStackTrace();
        }

        Runtime.getRuntime().addShutdownHook(new Thread() {

            public void run() {
                System.out.println("Stoppping...");
                senderThread.interrupt();
                receiverThread.interrupt();
            }
        });

        Thread.sleep(8000);
    }

}