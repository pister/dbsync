package com.github.pister.dbsync.common.security;



import com.github.pister.dbsync.common.tools.util.IoUtil;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import java.io.*;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * User: huangsongli
 * Date: 16/2/29
 * Time: 上午9:25
 */
public class RSAUtil {

    private static final String KEY_ALGORITHM = "RSA";

    /**
     * 生成公钥和私钥
     */
    public static KeyPair generateKeys(int keySize) throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(KEY_ALGORITHM);
        keyPairGen.initialize(keySize);
        return keyPairGen.generateKeyPair();
    }

    /**
     * 生成公钥和私钥
     * keySize: 1024
     */
    public static KeyPair generateKeys() throws NoSuchAlgorithmException {
        return generateKeys(1024);
    }

    /**
     * PKCS#8格式
     *
     * @param keyData
     * @return
     */
    public static RSAPrivateKey getPrivateKey(byte[] keyData) {
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(keyData);
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
            return (RSAPrivateKey) keyFactory.generatePrivate(pkcs8EncodedKeySpec);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * x.509格式
     *
     * @param keyData
     * @return
     */
    public static RSAPublicKey getPublicKey(byte[] keyData) {
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(keyData);
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
            return (RSAPublicKey) keyFactory.generatePublic(x509EncodedKeySpec);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * x.509格式
     *
     * @param keyPath
     * @return
     */
    public static RSAPublicKey getPublicKeyFromPath(String keyPath) {
        File file = new File(keyPath);
        if (!file.exists()) {
            return null;
        }
        try {
            return getPublicKey(IoUtil.readAsBytes(file));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * PKCS#8格式
     *
     * @param keyPath
     * @return
     */
    public static RSAPrivateKey getPrivateKeyFromPath(String keyPath) {
        File file = new File(keyPath);
        if (!file.exists()) {
            return null;
        }
        try {
            return getPrivateKey(IoUtil.readAsBytes(file));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] encryptByPublicKey(byte[] data, RSAPublicKey publicKey) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(128);
        encryptByPublicKey(new ByteArrayInputStream(data), bos, publicKey);
        return bos.toByteArray();
    }

    public static byte[] decryptByPrivateKey(byte[] data, RSAPrivateKey privateKey) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(128);
        decryptByPrivateKey(new ByteArrayInputStream(data), bos, privateKey);
        return bos.toByteArray();
    }

    public static void encryptByPublicKey(InputStream rawData, OutputStream encryptedData, RSAPublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        CipherOutputStream cipherOutputStream = new CipherOutputStream(encryptedData, cipher);
        IoUtil.copyAndClose(rawData, cipherOutputStream);
    }

    public static void decryptByPrivateKey(InputStream encryptedData, OutputStream rawData, RSAPrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        CipherInputStream cipherInputStream = new CipherInputStream(encryptedData, cipher);
        IoUtil.copyAndClose(cipherInputStream, rawData);
    }

}
