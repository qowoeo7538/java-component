package org.shaw.io;

import java.io.IOException;
import java.io.Writer;

/**
 * @create: 2018-03-05
 * @description:
 */
public class UnsafeStringWriter extends Writer {

    /** 将字符串维护到该变量中. */
    private StringBuilder mBuffer;

    public UnsafeStringWriter() {
        lock = mBuffer = new StringBuilder();
    }

    public UnsafeStringWriter(int size) {
        if (size < 0) {
            throw new IllegalArgumentException("Negative buffer size");
        }
        lock = mBuffer = new StringBuilder();
    }

    /**
     * 将 int 写入 {@link #mBuffer}
     *
     * @param c {@code int}
     */
    @Override
    public void write(int c) {
        mBuffer.append((char) c);
    }

    /**
     * 将 char[] 写入 {@link #mBuffer}
     *
     * @param cs {@code int}
     */
    @Override
    public void write(char[] cs) throws IOException {
        mBuffer.append(cs, 0, cs.length);
    }

    /**
     * 将 char[] 写入 {@link #mBuffer}
     *
     * @param cs  写入 char[]
     * @param off 写入的位置
     * @param len 写入的长度
     * @throws IOException
     */
    @Override
    public void write(char[] cs, int off, int len) throws IOException {
        if ((off < 0) || (off > cs.length) || (len < 0) ||
                ((off + len) > cs.length) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        }
        if (len > 0) {
            mBuffer.append(cs, off, len);
        }
    }

    /**
     * 将 String 写入 {@link #mBuffer}
     *
     * @param str 写入 String
     */
    @Override
    public void write(String str) {
        mBuffer.append(str);
    }

    /**
     * 将 String 写入 {@link #mBuffer}
     *
     * @param str 写入 String
     * @param off 写入的位置
     * @param len 写入的长度
     */
    @Override
    public void write(String str, int off, int len) {
        mBuffer.append(str.substring(off, off + len));
    }

    /**
     * 将 CharSequence 写入 {@link #mBuffer}
     *
     * @param csq {@code CharSequence}
     */
    @Override
    public Writer append(CharSequence csq) {
        if (csq == null) {
            write("null");
        } else {
            write(csq.toString());
        }
        return this;
    }

    /**
     * 截取 {@code CharSequence} start-end 位置的信息写入 {@link #mBuffer}
     *
     * @param csq   CharSequence
     * @param start CharSequence 开始位置
     * @param end   CharSequence 结束位置
     */
    @Override
    public Writer append(CharSequence csq, int start, int end) {
        CharSequence cs = (csq == null ? "null" : csq);
        write(cs.subSequence(start, end).toString());
        return this;
    }

    /**
     * 将 char 写入 {@link #mBuffer}
     *
     * @param c char
     * @return
     */
    @Override
    public Writer append(char c) {
        mBuffer.append(c);
        return this;
    }

    @Override
    public void flush() throws IOException {

    }

    @Override
    public void close() throws IOException {

    }

    @Override
    public String toString() {
        return mBuffer.toString();
    }
}
