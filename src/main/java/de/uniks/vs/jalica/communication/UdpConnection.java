package de.uniks.vs.jalica.communication;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

public class UdpConnection {

    private static final String INADDR_ANY = "0.0.0.0";
    private final SocketAddress socketAddress;
    private InetAddress outAddress;
    private InetAddress broadcast;
    private DatagramChannel handle;
    private int port;

    public UdpConnection(int port) throws IOException {
        this.port = port;

        //  Create UDP socket
//        this.handle = socket (AF_INET, SOCK_DGRAM, IPPROTO_UDP);
         this.handle = DatagramChannel.open();
        //  Ask operating system teamObserver let us do broadcasts from socket
//        this.handle.setOption(StandardSocketOptions.SO_BROADCAST, true);

        NetworkInterface in = NetworkInterface.getByIndex(8);
        this.handle.setOption(StandardSocketOptions.IP_MULTICAST_IF, in);
        //  Allow multiple processes teamObserver bind teamObserver socket; incoming
        //  messages will come teamObserver each process
        this.handle.setOption(StandardSocketOptions.SO_REUSEADDR, true);
        this.handle.configureBlocking(false);

        ByteBuffer buffer = ByteBuffer.allocate(1);

        socketAddress = new SocketAddress();
        socketAddress.inPort = this.port;
        socketAddress.inAdress = INADDR_ANY;
        socketAddress.inFamily = StandardProtocolFamily.INET;
        this.handle.bind(new InetSocketAddress(InetAddress.getByName(socketAddress.inAdress), socketAddress.inPort));

        Enumeration enumeration = NetworkInterface.getNetworkInterfaces();

        while(enumeration.hasMoreElements()) {
            NetworkInterface networkInterface = (NetworkInterface) enumeration.nextElement();
            Enumeration enumeration1 = networkInterface.getInetAddresses();

            while (enumeration1.hasMoreElements()) {
                InetAddress inetAddress = (InetAddress) enumeration1.nextElement();
                this.outAddress = inetAddress;

                List<InterfaceAddress> list = networkInterface.getInterfaceAddresses();
                Iterator<InterfaceAddress> it = list.iterator();

                while (it.hasNext()) {
                    InterfaceAddress ia = it.next();

                    if(ia.getBroadcast() != null)
                        this.broadcast = ia.getBroadcast();
                }
            }
        }
    }

    public DatagramChannel handle() {
        return this.handle;
    }

    public java.net.SocketAddress recv(ByteBuffer buffer) throws IOException {
        InetSocketAddress sourceAddr = (InetSocketAddress) handle.receive(buffer);
        System.out.printf("Found peer %s:%d\n", sourceAddr.getAddress(), sourceAddr.getPort());
        return sourceAddr;
    }

    public void send(ByteBuffer buffer) throws IOException {
//        handle.send(buffer, new InetSocketAddress("255.255.255.255", port));
        System.out.println("send "+ outAddress + ":" +port);
        System.out.println("send "+ socketAddress.inAdress + ":" + socketAddress.inPort);
        handle.send(buffer, new InetSocketAddress(outAddress, port));
    }

    public void destroy() {

    }

    private class SocketAddress {
        public Object inFamily;
        public int inPort;
        public String inAdress;
    }
}
