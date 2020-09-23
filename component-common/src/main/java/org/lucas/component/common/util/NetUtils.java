package org.lucas.component.common.util;

import org.springframework.util.StringUtils;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

/**
 * 网络工具
 */
public abstract class NetUtils {

    private static volatile String localHostAddress;

    private static volatile String localIp;

    private static final String LOCAL_HOST = "127.0.0.1";

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
     * 获取Ip地址 xxx.xxx.xxx.xxx
     *
     * @return
     */
    public static String getLocalIp() throws UnknownHostException, SocketException {
        if (!StringUtils.isEmpty(localIp)) {
            return localIp;
        }
        synchronized (NetUtils.class) {
            if (!StringUtils.isEmpty(localIp)) {
                return localIp;
            }
            String ip = getLocalHostAddress();
            String[] ipArray = ip.split("\\.");
            StringBuilder ipBuf = new StringBuilder();
            for (int j = 0; j < 4; j++) {
                for (int i = ipArray[j].length(); i < 3; i++) {
                    ipBuf.append('0');
                }
                ipBuf.append(ipArray[j]);
            }
            localIp = ipBuf.toString();
        }
        return localIp;
    }

    /**
     * 取本机IP地址。
     *
     * @return 本机IP地址 -Djava.net.preferIPv4Stack=TRUE
     */
    private static String getLocalHostAddress() throws UnknownHostException, SocketException {
        if (!StringUtils.isEmpty(localHostAddress)) {
            return localHostAddress;
        }
        String ip;
        String ipBak = "";
        InetAddress inetAddress;
        inetAddress = InetAddress.getLocalHost();
        ip = inetAddress.getHostAddress();
        if (!StringUtils.isEmpty(ip) && !LOCAL_HOST.equals(ip) && ip.indexOf(':') < 0) {
            localHostAddress = ip;
            return ip;
        }
        Enumeration netInterfaces;
        netInterfaces = NetworkInterface.getNetworkInterfaces();
        InetAddress iAddress;
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
                    if (!ip.equals(LOCAL_HOST) && ip.split("\\.").length == 4
                            && ip.indexOf(':') < 0) {
                        ipBak = ip;
                    }
                    ip = "";
                }
            }
        }

        if (!ip.equals(LOCAL_HOST) && ip.split("\\.").length == 4
                && ip.indexOf(':') < 0) {
            localHostAddress = ip;
            return ip;
        }
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
                    if (!ia.isSiteLocalAddress() && !ip.equals(LOCAL_HOST)
                            && ip.split("\\.").length == 4
                            && ip.indexOf(':') < 0) {
                        localHostAddress = ip;
                        return ip;
                    }

                    if (ni.getName().equals("eth1")
                            && !ia.isSiteLocalAddress()
                            && !ip.equals(LOCAL_HOST)
                            && ip.split("\\.").length == 4
                            && ip.indexOf(':') < 0) {
                        ipBak = ip;
                        ip = "";
                    }

                }
                break;
            }
        }
        if (!ip.equals(LOCAL_HOST) && ip.split("\\.").length == 4
                && ip.indexOf(':') < 0) {
            localHostAddress = ip;
            return ip;
        }
        if (!ipBak.equals(LOCAL_HOST) && ipBak.split("\\.").length == 4
                && ipBak.indexOf(':') < 0) {
            localHostAddress = ipBak;
            return ipBak;
        }
        localHostAddress = ip;
        return ip;
    }

}
