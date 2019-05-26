package de.uniks.vs.jalica.discovery;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Enumeration;

public class DatagramChannelMulticastPublisher {

    private String MULTICAST_IP;
    private String MULTICAST_INTERFACE;
    private int MULTICAST_PORT;

    public void sendMessage(String ip, int port, String multicastInterface, String message) throws IOException {
        this.MULTICAST_IP = ip;
        this.MULTICAST_PORT = port;
        this.MULTICAST_INTERFACE = multicastInterface;

        DatagramChannel datagramChannel=DatagramChannel.open();
        datagramChannel.bind(null);

        NetworkInterface networkInterface = NetworkInterface.getByName(MULTICAST_INTERFACE);

//        NetworkInterface networkInterface = NetworkInterface.getByIndex(1);
        Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
//        InetAddress inetAddress = inetAddresses.nextElement();

        datagramChannel.setOption(StandardSocketOptions.IP_MULTICAST_IF, networkInterface);
        ByteBuffer byteBuffer=ByteBuffer.wrap(message.getBytes());
        InetSocketAddress inetSocketAddress = new InetSocketAddress(MULTICAST_IP, MULTICAST_PORT);
        datagramChannel.send(byteBuffer,inetSocketAddress);
    }
}
