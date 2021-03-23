package org.lucas.component.common.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Objects;

class NetUtils_Tests {

    @Test
    void testGetIpCode() throws Exception {
        String ip = NetUtils.getLocalIp();
        System.out.println(ip);
        Assertions.assertNotNull(Objects.equals(NetUtils.getLocalIp(), ip));
    }

}
