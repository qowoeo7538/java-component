package org.shaw.util;

/**
 * 维护一个有效值的助手类
 */
public class Holder<T> {

    private volatile T value;

    public void set(T value) {
        this.value = value;
    }

    public T get() {
        return value;
    }
}
