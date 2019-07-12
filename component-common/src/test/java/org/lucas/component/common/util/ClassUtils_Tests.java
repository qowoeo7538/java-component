package org.lucas.component.common.util;

import org.junit.jupiter.api.Test;

/**
 * @create: 2018-06-23
 * @description:
 */
public class ClassUtils_Tests {

    //  匿名类
    Object anonymous = (Comparable<String>) (o) -> 0;

    // 成员类
    private class MemberClass {

    }

    @Test
    public void testGetTypeStr() {

        // 本地类
        class LocalType {

        }

    }
}

class StaticClass {

}
