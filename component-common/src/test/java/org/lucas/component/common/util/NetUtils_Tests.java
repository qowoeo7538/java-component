package org.lucas.component.common.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Objects;

public class NetUtils_Tests {

    @Test
    public void testGetIpCode() {
        String ip = NetUtils.getLocalIp();
        System.out.println(ip);
        Assertions.assertNotNull(Objects.equals(NetUtils.getLocalIp(), ip));
    }

}
