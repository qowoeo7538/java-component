package org.lucas.component.common.util;

import org.lucas.component.common.io.support.ReadProcess;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.Base64;

/**
 * io工具类
 */
public abstract class StreamUtils {

    /**
     * 通道处理工具类
     *
     * @param channel     通道
     * @param readProcess 处理对象
     * @throws IOException
     */
    public static void channelRead(final ScatteringByteChannel channel, final ReadProcess readProcess) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(4096);
        while (channel.read(buffer) > 0) {
            buffer.flip();
            while (buffer.hasRemaining()) {
                readProcess.onProcess(buffer);
            }
            buffer.clear();
        }
    }

    /**
     * char转换byte[]
     *
     * @param chars  字符
     * @param encode 编码
     * @return 字节
     */
    public static byte[] getCharToByte(char chars, String encode) {
        Charset cs = Charset.forName(encode);
        //分配缓冲区
        CharBuffer cb = CharBuffer.allocate(1);
        //放入缓冲区
        cb.put(chars);
        //为一系列通道写入或相对获取 操作做好准备
        cb.flip();
        //将此 charset 中的字符串编码成字节的便捷方法。
        ByteBuffer bb = cs.encode(cb);
        return bb.array();
    }

    /**
     * @param fileName 文件路径名
     * @return 以字节为单位返回文件大小;
     */
    public static long getFileSize(String fileName) throws Exception {
        File file = new File(fileName);
        FileInputStream fileInputStream = new FileInputStream(file);
        FileChannel fileChannel = fileInputStream.getChannel();
        // return file.length();
        return fileChannel.size();
    }

    /**
     * 计算文件的哈希值
     *
     * @param file
     * @param hashType "MD5"，"SHA1","SHA-1"，"SHA-256"，"SHA-384"，"SHA-512"
     * @return
     */
    public static String getFileMd5(File file, String hashType) {
        if (!file.isFile()) {
            return null;
        }
        MessageDigest digest;
        byte buffer[] = new byte[8192];
        int len;
        try (FileInputStream in = new FileInputStream(file)) {
            digest = MessageDigest.getInstance(hashType);
            while ((len = in.read(buffer)) != -1) {
                digest.update(buffer, 0, len);
            }
            BigInteger bigInt = new BigInteger(1, digest.digest());
            return bigInt.toString(16);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 文件字节转字符串
     *
     * @param file 文件路径
     * @return 以 base64 的方式编码转成字符串
     * @throws Exception
     */
    public static String byteToString(File file) throws IOException {
        StringBuilder returnDatas = new StringBuilder();
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            Base64.Encoder encoder = Base64.getEncoder();
            byte[] buf = new byte[10 * 1024];
            int readLenth;
            while ((readLenth = fileInputStream.read(buf)) != -1) {
                byte[] copyByte = new byte[readLenth - 1];
                for (int i = 0; i < copyByte.length; i++) {
                    copyByte[i] = buf[i];
                }
                returnDatas.append(encoder.encode(copyByte));
            }
        }
        return returnDatas.toString();
    }

    /**
     * 将以 base64 的字符串转换成文件
     *
     * @param str  字符串
     * @param file 文件
     * @throws Exception
     */
    public static void strToByte(String str, File file) throws IOException {
        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            Base64.Decoder decoder = Base64.getDecoder();
            fileOutputStream.write(decoder.decode(str));
        }
    }
}
