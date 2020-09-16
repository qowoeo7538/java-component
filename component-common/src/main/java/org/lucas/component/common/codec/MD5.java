package org.lucas.component.common.codec;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public abstract class MD5 {

    public static String encodeBase64(String data, boolean urlSafe) throws NoSuchAlgorithmException {
        try {
            return encodeBase64(data.getBytes("utf8"), urlSafe);
        } catch (UnsupportedEncodingException var3) {
            return null;
        }
    }

    public static String encodeBase64(byte[] data, boolean urlSafe) throws NoSuchAlgorithmException {
        byte[] md5Code = rawEncode(data);
        return md5Code != null ? new String(urlSafe ? Base64.getUrlEncoder().encode(md5Code) : Base64.getEncoder().encode(md5Code), 0, 22) : null;
    }

    public static byte[] rawEncode(byte[] data) throws NoSuchAlgorithmException {
        MessageDigest MD5 = MessageDigest.getInstance("MD5");
        MD5.update(data, 0, data.length);
        return MD5.digest();
    }

}
