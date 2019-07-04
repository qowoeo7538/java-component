package org.lucas.component.common.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Array;

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

        Assertions.assertEquals(ClassUtils.getTypeStr(String.class), "java.lang.String");

        Assertions.assertEquals(ClassUtils.getTypeStr(boolean.class), "boolean");
        Assertions.assertEquals(ClassUtils.getTypeStr(byte.class), "byte");
        Assertions.assertEquals(ClassUtils.getTypeStr(char.class), "char");
        Assertions.assertEquals(ClassUtils.getTypeStr(double.class), "double");
        Assertions.assertEquals(ClassUtils.getTypeStr(float.class), "float");
        Assertions.assertEquals(ClassUtils.getTypeStr(int.class), "int");
        Assertions.assertEquals(ClassUtils.getTypeStr(long.class), "long");
        Assertions.assertEquals(ClassUtils.getTypeStr(short.class), "short");
        Assertions.assertEquals(ClassUtils.getTypeStr(void.class), "void");

        // 本地类
        class LocalType {

        }

        Assertions.assertEquals(ClassUtils.getTypeStr(anonymous.getClass()),
                "org.lucas.util.ClassTypeUtilsTest$1");
        Assertions.assertEquals(ClassUtils.getTypeStr(LocalType.class),
                "org.lucas.util.ClassTypeUtilsTest$2LocalType");
        Assertions.assertEquals(ClassUtils.getTypeStr(MemberClass.class),
                "org.lucas.util.ClassTypeUtilsTest$MemberClass");
        Assertions.assertEquals(ClassUtils.getTypeStr(StaticClass.class),
                "StaticClass");

        Assertions.assertEquals(ClassUtils.getTypeStr(String[][][].class), "java.lang.String[][][]");
        Assertions.assertEquals(ClassUtils.getTypeStr(boolean[].class), "boolean[]");
        Assertions.assertEquals(ClassUtils.getTypeStr(byte[].class), "byte[]");
        Assertions.assertEquals(ClassUtils.getTypeStr(char[].class), "char[]");
        Assertions.assertEquals(ClassUtils.getTypeStr(double[].class), "double[]");
        Assertions.assertEquals(ClassUtils.getTypeStr(float[].class), "float[]");
        Assertions.assertEquals(ClassUtils.getTypeStr(int[].class), "int[]");
        Assertions.assertEquals(ClassUtils.getTypeStr(long[].class), "long[]");
        Assertions.assertEquals(ClassUtils.getTypeStr(short[].class), "short[]");
        Assertions.assertEquals(ClassUtils.getTypeStr(Array.newInstance(anonymous.getClass(), 2, 3).getClass()),
                "org.lucas.util.ClassTypeUtilsTest$1[][]");
        Assertions.assertEquals(ClassUtils.getTypeStr(LocalType[][].class),
                "org.lucas.util.ClassTypeUtilsTest$2LocalType[][]");
        Assertions.assertEquals(ClassUtils.getTypeStr(MemberClass[].class),
                "org.lucas.util.ClassTypeUtilsTest$MemberClass[]");
        Assertions.assertEquals(ClassUtils.getTypeStr(StaticClass[].class),
                "StaticClass[]");

        Assertions.assertArrayEquals(ClassUtils.getTypeStrs(new Class[]{String[].class}),
                new String[]{"java.lang.String[]"});
        Assertions.assertArrayEquals(ClassUtils.getTypeStrs(new Class[]{String[].class}, false),
                new String[]{"java.lang.String[]"});
        Assertions.assertArrayEquals(ClassUtils.getTypeStrs(new Class[]{String[].class}, true),
                new String[]{String[].class.getName()});

    }
}

class StaticClass {

}
