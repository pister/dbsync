package com.github.pister.dbsync.common.security;

import com.github.pister.dbsync.common.tools.util.IoUtil;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class BlowFishUtil {

	private static final String CIPHER_NAME = "Blowfish/CFB8/NoPadding";
	private static final String KEY_SPEC_NAME = "Blowfish";

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

	public static OutputStream encryptOutputStream(byte[] key, OutputStream outData) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, InvalidAlgorithmParameterException {
        byte[] md5Key = DigestUtil.md5(key);
        byte[] iv = new byte[8];
        byte[] secret = new byte[16];
        System.arraycopy(md5Key, 0, iv, 0, 8);
        System.arraycopy(md5Key, 0, secret, 0, 16);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
        SecretKeySpec secretKeySpec = new SecretKeySpec(secret, KEY_SPEC_NAME);
        Cipher enCipher = Cipher.getInstance(CIPHER_NAME);
        enCipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
		return new CipherOutputStream(outData, enCipher);
	}

	public static InputStream decryptInputStream(byte[] key, InputStream inData) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, InvalidAlgorithmParameterException {
        byte[] md5Key = DigestUtil.md5(key);
        byte[] iv = new byte[8];
        byte[] secret = new byte[16];
        System.arraycopy(md5Key, 0, iv, 0, 8);
        System.arraycopy(md5Key, 0, secret, 0, 16);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
        SecretKeySpec secretKeySpec = new SecretKeySpec(secret, KEY_SPEC_NAME);
        Cipher deCipher = Cipher.getInstance(CIPHER_NAME);
        deCipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
		return new CipherInputStream(inData, deCipher);
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
