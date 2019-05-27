package de.uniks.vs.jalica.engine.common;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
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

    public static void getAvaibleNetworkInterfaces() {
        try {

            Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();

            while (e.hasMoreElements()) {
                NetworkInterface ni = e.nextElement();
                System.out.println("Net interface: " + ni.getName() + " - " + ni.getDisplayName());

                Enumeration<InetAddress> e2 = ni.getInetAddresses();

                while (e2.hasMoreElements()) {
                    InetAddress ip = e2.nextElement();

                    System.out.println("IP address: " + ip.toString() + "   "+ ip.isMulticastAddress());
                }
                System.out.println();
            }

            InetAddress[] addresses = InetAddress.getAllByName(InetAddress.getLocalHost().getHostAddress());

            for (InetAddress address:addresses) {
                System.out.println(address.getHostName());
                System.out.println(address);
            }
        } catch (UnknownHostException | SocketException e) {
            e.printStackTrace();
        }
    }
}
