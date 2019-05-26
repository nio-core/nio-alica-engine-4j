package de.uniks.vs.jalica.discovery;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.MembershipKey;
import java.util.Enumeration;

public class DatagramChannelMulticastReceiver {

    private String MULTICAST_IP;
    private String MULTICAST_INTERFACE;
    private int MULTICAST_PORT;

    public String receiveMessage(String ip, int port, String multicastInterface) throws IOException {
        this.MULTICAST_IP = ip;
        this.MULTICAST_PORT = port;
        this.MULTICAST_INTERFACE = multicastInterface;

        DatagramChannel datagramChannel = DatagramChannel.open(StandardProtocolFamily.INET);
        NetworkInterface networkInterface = NetworkInterface.getByName(MULTICAST_INTERFACE);

//        NetworkInterface networkInterface = NetworkInterface.getByIndex(1);
        Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
//        InetAddress inetAddress = inetAddresses.nextElement();

        datagramChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
        datagramChannel.bind(new InetSocketAddress(MULTICAST_PORT));
        datagramChannel.setOption(StandardSocketOptions.IP_MULTICAST_IF, networkInterface);
        InetAddress inetAddress = InetAddress.getByName(MULTICAST_IP);
        MembershipKey membershipKey = datagramChannel.join(inetAddress, networkInterface);
        System.out.println("Waiting for the message...");

        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

        datagramChannel.receive(byteBuffer);
        System.out.println("Message received");
        byteBuffer.flip();
        byte[] bytes = new byte[byteBuffer.limit()];
        byteBuffer.get(bytes, 0, byteBuffer.limit());
        membershipKey.drop();
        return new String(bytes);
    }
}
