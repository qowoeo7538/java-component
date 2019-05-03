package org.lucas.util;

import org.junit.Assert;
import org.junit.Test;

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

        Assert.assertEquals(ClassUtils.getTypeStr(String.class), "java.lang.String");

        Assert.assertEquals(ClassUtils.getTypeStr(boolean.class), "boolean");
        Assert.assertEquals(ClassUtils.getTypeStr(byte.class), "byte");
        Assert.assertEquals(ClassUtils.getTypeStr(char.class), "char");
        Assert.assertEquals(ClassUtils.getTypeStr(double.class), "double");
        Assert.assertEquals(ClassUtils.getTypeStr(float.class), "float");
        Assert.assertEquals(ClassUtils.getTypeStr(int.class), "int");
        Assert.assertEquals(ClassUtils.getTypeStr(long.class), "long");
        Assert.assertEquals(ClassUtils.getTypeStr(short.class), "short");
        Assert.assertEquals(ClassUtils.getTypeStr(void.class), "void");

        // 本地类
        class LocalType {

        }

        Assert.assertEquals(ClassUtils.getTypeStr(anonymous.getClass()),
                "org.lucas.util.ClassTypeUtilsTest$1");
        Assert.assertEquals(ClassUtils.getTypeStr(LocalType.class),
                "org.lucas.util.ClassTypeUtilsTest$2LocalType");
        Assert.assertEquals(ClassUtils.getTypeStr(MemberClass.class),
                "org.lucas.util.ClassTypeUtilsTest$MemberClass");
        Assert.assertEquals(ClassUtils.getTypeStr(StaticClass.class),
                "StaticClass");

        Assert.assertEquals(ClassUtils.getTypeStr(String[][][].class), "java.lang.String[][][]");
        Assert.assertEquals(ClassUtils.getTypeStr(boolean[].class), "boolean[]");
        Assert.assertEquals(ClassUtils.getTypeStr(byte[].class), "byte[]");
        Assert.assertEquals(ClassUtils.getTypeStr(char[].class), "char[]");
        Assert.assertEquals(ClassUtils.getTypeStr(double[].class), "double[]");
        Assert.assertEquals(ClassUtils.getTypeStr(float[].class), "float[]");
        Assert.assertEquals(ClassUtils.getTypeStr(int[].class), "int[]");
        Assert.assertEquals(ClassUtils.getTypeStr(long[].class), "long[]");
        Assert.assertEquals(ClassUtils.getTypeStr(short[].class), "short[]");
        Assert.assertEquals(ClassUtils.getTypeStr(Array.newInstance(anonymous.getClass(), 2, 3).getClass()),
                "org.lucas.util.ClassTypeUtilsTest$1[][]");
        Assert.assertEquals(ClassUtils.getTypeStr(LocalType[][].class),
                "org.lucas.util.ClassTypeUtilsTest$2LocalType[][]");
        Assert.assertEquals(ClassUtils.getTypeStr(MemberClass[].class),
                "org.lucas.util.ClassTypeUtilsTest$MemberClass[]");
        Assert.assertEquals(ClassUtils.getTypeStr(StaticClass[].class),
                "StaticClass[]");

        Assert.assertArrayEquals(ClassUtils.getTypeStrs(new Class[]{String[].class}),
                new String[]{"java.lang.String[]"});
        Assert.assertArrayEquals(ClassUtils.getTypeStrs(new Class[]{String[].class}, false),
                new String[]{"java.lang.String[]"});
        Assert.assertArrayEquals(ClassUtils.getTypeStrs(new Class[]{String[].class}, true),
                new String[]{String[].class.getName()});

    }
}

class StaticClass {

}
