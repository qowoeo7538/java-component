package org.lucas.component.common.util;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Enumeration;

/**
 * 网络工具
 */
public abstract class NetUtils {

    private static volatile String LOCAL_HOST_ADDRESS = "";

    private static volatile String LOCAL_IP = "";

    /**
     * 通过 host 获取 IP 地址
     *
     * @param hostName host 地址
     * @return IP 地址
     */
    public static String getIpByHost(final String hostName) {
        try {
            return InetAddress.getByName(hostName).getHostAddress();
        } catch (final UnknownHostException e) {
            return hostName;
        }
    }

    /**
     * 获取Ip地址
     *
     * @return
     */
    public static String getLocalIp() {
        if (!StringUtils.isEmpty(LOCAL_IP)) {
            return LOCAL_IP;
        }
        synchronized (NetUtils.class) {
            if (!StringUtils.isEmpty(LOCAL_IP)) {
                return LOCAL_IP;
            }
            String ip = getLocalHostAddress();
            String[] ipArray = ip.split("\\.");
            for (int j = 0; j < 4; j++) {
                for (int i = ipArray[j].length(); i < 3; i++) {
                    LOCAL_IP = LOCAL_IP + "0";
                }
                LOCAL_IP = LOCAL_IP + ipArray[j];
            }
        }
        return LOCAL_IP;
    }

    /**
     * 取本机IP地址。
     *
     * @return 本机IP地址 -Djava.net.preferIPv4Stack=TRUE
     */
    private static String getLocalHostAddress() {
        if (!StringUtils.isEmpty(LOCAL_HOST_ADDRESS)) {
            return LOCAL_HOST_ADDRESS;
        }
        String ip = "";
        String ipBak = "";
        InetAddress inetAddress = null;
        try {
            inetAddress = InetAddress.getLocalHost();
            ip = inetAddress.getHostAddress();
        } catch (Throwable e1) {
            e1.printStackTrace();
        }
        if (!StringUtils.isEmpty(ip) && !"127.0.0.1".equals(ip) && ip.indexOf(':') < 0) {
            LOCAL_HOST_ADDRESS = ip;
            return ip;
        }
        Enumeration netInterfaces = null;
        try {
            netInterfaces = NetworkInterface.getNetworkInterfaces();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        InetAddress iAddress = null;
        try {
            out:
            while (netInterfaces.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) netInterfaces
                        .nextElement();

                Enumeration<InetAddress> inetAddresses = ni.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    iAddress = inetAddresses.nextElement();
                    if (!iAddress.isSiteLocalAddress()
                            && !iAddress.isLoopbackAddress()
                            && iAddress.getHostAddress().indexOf(':') == -1) {
                        ip = iAddress.getHostAddress();
                        break out;
                    } else {
                        ip = iAddress.getHostAddress();
                        if (!ip.equals("127.0.0.1") && ip.split("\\.").length == 4
                                && ip.indexOf(':') < 0) {
                            ipBak = ip;
                        }
                        ip = "";
                    }
                }
            }
        } catch (Throwable e3) {
            e3.printStackTrace();

        }
        if (!ip.equals("127.0.0.1") && ip.split("\\.").length == 4
                && ip.indexOf(':') < 0) {
            LOCAL_HOST_ADDRESS = ip;
            return ip;
        }
        try {
            Enumeration<?> e1 = NetworkInterface
                    .getNetworkInterfaces();
            while (e1.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) e1.nextElement();
                if (!ni.getName().equals("eth0")
                        && !ni.getName().equals("eth1")
                        && !ni.getName().equals("bond0")) {
                    continue;
                } else {
                    Enumeration<?> e2 = ni.getInetAddresses();
                    while (e2.hasMoreElements()) {
                        InetAddress ia = (InetAddress) e2.nextElement();
                        if (ia instanceof Inet6Address) {
                            continue;
                        }
                        ip = ia.getHostAddress();
                        if (!ia.isSiteLocalAddress() && !ip.equals("127.0.0.1")
                                && ip.split("\\.").length == 4
                                && ip.indexOf(':') < 0) {
                            LOCAL_HOST_ADDRESS = ip;
                            return ip;
                        }

                        if (ni.getName().equals("eth1")
                                && !ia.isSiteLocalAddress()
                                && !ip.equals("127.0.0.1")
                                && ip.split("\\.").length == 4
                                && ip.indexOf(':') < 0) {
                            ipBak = ip;
                            ip = "";
                        }

                    }
                    break;
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        if (!ip.equals("127.0.0.1") && ip.split("\\.").length == 4
                && ip.indexOf(':') < 0) {
            LOCAL_HOST_ADDRESS = ip;
            return ip;
        }
        if (!ipBak.equals("127.0.0.1") && ipBak.split("\\.").length == 4
                && ipBak.indexOf(':') < 0) {
            LOCAL_HOST_ADDRESS = ipBak;
            return ipBak;
        }
        LOCAL_HOST_ADDRESS = ip;
        return ip;
    }

}
