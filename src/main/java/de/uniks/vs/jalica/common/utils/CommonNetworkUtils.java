package de.uniks.vs.jalica.common.utils;

import de.uniks.vs.jalica.common.udp.UDPMulticastReceiver;
import de.uniks.vs.jalica.common.udp.UDPMulticastSender;
import sun.net.util.IPAddressUtil;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;


public class CommonNetworkUtils {

    public static InetAddress localAddress() {
        String ip;
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                if (iface.isLoopback() || !iface.isUp())
                    continue;

                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while(addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    ip = addr.getHostAddress();
                    if (ip.startsWith("10.") || ip.startsWith("172.31.") || ip.startsWith("192.168"))
                        return addr;
                }
            }
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public static boolean isLocalhost(String hostAddress) {
        boolean isLocalhost = false;
        Enumeration<NetworkInterface> networkInterfaces;
        try {
            networkInterfaces = NetworkInterface.getNetworkInterfaces();

            if (networkInterfaces != null) {

                OUTER:
                while (networkInterfaces.hasMoreElements()) {
                    NetworkInterface networkInterface = networkInterfaces.nextElement();
                    Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();

                    if (inetAddresses != null) {

                        while (inetAddresses.hasMoreElements()) {
                            InetAddress inetAddress = inetAddresses.nextElement();

                            if (hostAddress.equals(inetAddress.getHostAddress())) {
                                isLocalhost = true;
                                break OUTER;
                            }
                        }
                    }
                }
            }
        }
        catch (SocketException e) {
            e.printStackTrace();
        }
        return isLocalhost;
    }

    public static String getOwnNetworkHostAdress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return "";
    }




    public static ArrayList<ArrayList<String>> getAvailableNetworkInterfaces() {

        ArrayList<ArrayList<String>> results = new ArrayList<>();

        try {

            Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();

            while (e.hasMoreElements()) {
                ArrayList<String> infos = new ArrayList<>();
                NetworkInterface ni = e.nextElement();
                System.out.println("CNU: Net interface: " + ni.getName() + " - " + ni.getDisplayName());
                infos.add(ni.getName());
                NetworkInterface networkInterface = NetworkInterface.getByName(ni.getName());
                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                InetAddress inetAddress = inetAddresses.nextElement();

                Enumeration<InetAddress> e2 = ni.getInetAddresses();

                while (e2.hasMoreElements()) {
                    InetAddress ip = e2.nextElement();
                    infos.add(ip.toString());
                    infos.add(String.valueOf(ip.isMulticastAddress()));
                    try {
                        infos.add(String.valueOf(ip.isReachable(10)));
                    } catch (IOException ex) {
                        System.out.println("CNU: not reachable");
                    }
                    infos.add(String.valueOf(testIsMulticastInterface(ni)));
                    System.out.println("CNU: IP address: " + ip.toString() + "   "+  isIPv4Address(ip.toString().replaceFirst("/", ""))
                    + "   " + IPAddressUtil.isIPv4LiteralAddress(ip.toString().replaceFirst("/", ""))+"   ");
                }
                results.add(infos);
                System.out.println();
            }

            InetAddress[] addresses = InetAddress.getAllByName(InetAddress.getLocalHost().getHostAddress());

            for (InetAddress address:addresses) {
                ArrayList<String> infos = new ArrayList<>();
                System.out.println("CNU: 2: " + address.getHostName());
                System.out.println("CNU: 2: " + address);
                infos.add(address.getHostName());
                infos.add(address.toString());
                results.add(infos);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return results;
    }
    public static ArrayList getAvailableMulticastNetworkInterfaces() {
        ArrayList multicastInterfaces = new ArrayList();

        try {
            Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();

            while (e.hasMoreElements()) {
                NetworkInterface ni = e.nextElement();

                if (testIsMulticastInterface(ni))
                     multicastInterfaces.add(ni);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return multicastInterfaces;
    }

    private static boolean testIsMulticastInterface(NetworkInterface ni) {
        final UDPMulticastReceiver receiverThread = new UDPMulticastReceiver(ni.getName(), "230.0.0.1", 4447);

        if (receiverThread.getSocketFailure())
            return false;
        receiverThread.start();
        final UDPMulticastSender senderThread = new UDPMulticastSender("230.0.0.1", 4447);

        if (!senderThread.send("PING"))
            return false;
        receiverThread.interrupt();
        senderThread.interrupt();
        return (receiverThread.getHosts().size() > 0 && (receiverThread.getHosts().values().contains("PING"))) ? true : false;
    }

    public static Boolean isIPv4Address(String address) {
//        IPAddressUtil.isIPv4LiteralAddress(address);
        if (address.isEmpty()) {
            return false;
        }
        try {
            Object res = InetAddress.getByName(address);
            return res instanceof Inet4Address;
        } catch (final UnknownHostException ex) {
            return false;
        }
    }

    public static Boolean isIPv6Address(String address) {
        //        IPAddressUtil.isIPv6LiteralAddress(address);
        if (address.isEmpty()) {
            return false;
        }
        try {
            Object res = InetAddress.getByName(address);
            return res instanceof Inet4Address;
        } catch (final UnknownHostException ex) {
            return false;
        }
    }
}
