package com.github.pister.dbsync.common.security;


import com.github.pister.dbsync.common.io.FastByteArrayInputStream;
import com.github.pister.dbsync.common.tools.codec.Hex;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by songlihuang on 2017/11/27.
 */
public class DigestUtil {

    private static final int BUF_SIZE = 1024 * 4;

    private static final ThreadLocal<byte[]> bufTL = new ThreadLocal<byte[]>() {
        @Override
        protected byte[] initialValue() {
            return new byte[BUF_SIZE];
        }
    };

    private static byte[] getBuffer() {
        return bufTL.get();
    }

    private static MessageDigest getAlgorithm(String algorithm) {
        try {
            return MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] doDigest(InputStream inputStream, MessageDigest messageDigest) throws IOException {
        // 如果考虑到messageDigest对象复用的情况：
        // 本来digest()方法后也会重置状态，但是考虑到有些情况在没有调用待digest时候出错了会导致意外状态
        messageDigest.reset();
        byte[] buf = getBuffer();
        while (true) {
            int len = inputStream.read(buf);
            if (len < 0) {
                break;
            }
            messageDigest.update(buf, 0, len);
        }
        return messageDigest.digest();
    }

    private static byte[] doDigest(byte[] data, MessageDigest messageDigest) {
        try {
            return doDigest(new FastByteArrayInputStream(data), messageDigest);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String bytesToHex(byte[] data) {
        return new String(Hex.encodeHex(data));
    }

    private static String doDigestHex(byte[] data, MessageDigest messageDigest) {
        return bytesToHex(doDigest(data, messageDigest));
    }

    private static String doDigestHex(InputStream inputStream, MessageDigest messageDigest) throws IOException {
        return bytesToHex(doDigest(inputStream, messageDigest));
    }

    public static String md5hex(InputStream inputStream) throws IOException {
        return doDigestHex(inputStream, getAlgorithm("MD5"));
    }

    public static byte[] md5(InputStream inputStream) throws IOException {
        return doDigest(inputStream, getAlgorithm("MD5"));
    }

    public static byte[] md5(byte[] data) {
        return doDigest(data, getAlgorithm("MD5"));
    }

    public static String md5hex(byte[] data) {
        return doDigestHex(data, getAlgorithm("MD5"));
    }

    public static String sha1hex(InputStream inputStream) throws IOException {
        return doDigestHex(inputStream, getAlgorithm("SHA-1"));
    }

    public static byte[] sha1(InputStream inputStream) throws IOException {
        return doDigest(inputStream, getAlgorithm("SHA-1"));
    }


    public static byte[] sha1(byte[] data) {
        return doDigest(data, getAlgorithm("SHA-1"));
    }

    public static String sha1hex(byte[] data) {
        return doDigestHex(data, getAlgorithm("SHA-1"));
    }

    public static String sha256hex(InputStream inputStream) throws IOException {
        return doDigestHex(inputStream, getAlgorithm("SHA-256"));
    }

    public static byte[] sha256(InputStream inputStream) throws IOException {
        return doDigest(inputStream, getAlgorithm("SHA-256"));
    }

    public static byte[] sha256(byte[] data) {
        return doDigest(data, getAlgorithm("SHA-256"));
    }

    public static String sha256hex(byte[] data) {
        return doDigestHex(data, getAlgorithm("SHA-256"));
    }

    public static String sha512hex(InputStream inputStream) throws IOException {
        return doDigestHex(inputStream, getAlgorithm("SHA-512"));
    }

    public static byte[] sha512(InputStream inputStream) throws IOException {
        return doDigest(inputStream, getAlgorithm("SHA-512"));
    }

    public static byte[] sha512(byte[] data) {
        return doDigest(data, getAlgorithm("SHA-512"));
    }

    public static String sha512hex(byte[] data) {
        return doDigestHex(data, getAlgorithm("SHA-512"));
    }


}
