package org.lucas.component.common.util;

import org.lucas.component.common.codec.MD5;

import java.security.NoSuchAlgorithmException;

public abstract class ShortUrlUtils {

    public static String shortenCodeUrlForSix(String longUrl) throws NoSuchAlgorithmException {
        return shortenCodeUrl(longUrl, 6);
    }

    public static String shortenCodeUrlForSeven(String longUrl) throws NoSuchAlgorithmException {
        return shortenCodeUrl(longUrl, 7);
    }

    public static String shortenCodeUrlForEight(String longUrl) throws NoSuchAlgorithmException {
        return shortenCodeUrl(longUrl, 8);
    }

    public static String shortenCodeUrl(String longUrl, int urlLength) throws NoSuchAlgorithmException {
        if (urlLength < 4) {
            urlLength = 8;
        }
        StringBuilder sbBuilder = new StringBuilder(urlLength + 2);
        String md5Hex = "";
        int nLen = 0;
        while (nLen < urlLength) {
            md5Hex = MD5.encodeBase64(md5Hex + longUrl, true);
            int md5Len = md5Hex.length();
            int copylen = md5Len < urlLength - nLen ? md5Len : urlLength - nLen;
            sbBuilder.append(md5Hex.substring(0, copylen));
            nLen += copylen;
            if (nLen == urlLength) {
                break;
            }
        }
        return sbBuilder.toString();
    }

}
