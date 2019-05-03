package org.lucas.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * 网络工具
 */
public abstract class NetUtils {

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

}
