package com.github.pister.dbsync.common.security;


import com.github.pister.dbsync.common.tools.util.IoUtil;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * User: huangsongli
 * Date: 16/2/29
 * Time: 下午7:41
 */
public class AESUtil {

    private static final String CIPHER_ALGORITHM_ECB = "AES/ECB/PKCS5Padding";
    private static final String KEY_ALGORITHM = "AES";
    // 使用AES加密时，当密钥大于128时，代码会抛出java.security.InvalidKeyException: Illegal key size or default parameters
    // copy /resource/patches/下的对应jdk版本的 local_policy.jar和US_export_policy.jar to $JDK_Home/jre/lib/security and $JRE_Home/lib/security

    public static SecretKey generateKey256() {
        return generateKey(256);
    }

    public static SecretKey generateKey128() {
        return generateKey(128);
    }

    public static SecretKey generateKey(int keySize) {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(keySize, new SecureRandom());
            return keyGenerator.generateKey();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] encrypt(byte[] key, byte[] inData) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(1024);
        encrypt(key, new ByteArrayInputStream(inData), bos);
        return bos.toByteArray();
    }

    public static byte[] decrypt(byte[] keyData, byte[] inData) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(1024);
        decrypt(keyData, new ByteArrayInputStream(inData), bos);
        return bos.toByteArray();
    }

    public static void encrypt(byte[] key, InputStream inData, OutputStream outData) {
        try {
            OutputStream outputStream = encryptOutputStream(key, outData);
            IoUtil.copyAndClose(inData, outputStream);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static OutputStream encryptOutputStream(byte[] key, OutputStream outData) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, KEY_ALGORITHM);
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM_ECB);
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
        CipherOutputStream cipherOutputStream = new CipherOutputStream(outData, cipher);
        return cipherOutputStream;
    }

    public static InputStream decryptInputStream(byte[] key, InputStream inData) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, KEY_ALGORITHM);
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM_ECB);
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
        CipherInputStream cipherInputStream = new CipherInputStream(inData, cipher);
        return cipherInputStream;
    }

    public static void decrypt(byte[] key, InputStream inData, OutputStream outData) {
        try {
            InputStream inputStream = decryptInputStream(key, inData);
            IoUtil.copyAndClose(inputStream, outData);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
