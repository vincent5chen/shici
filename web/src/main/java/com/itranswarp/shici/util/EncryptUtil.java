package com.itranswarp.shici.util;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class EncryptUtil {

	// Don't change the initial key:
	static final byte[] DEFAULT_KEY = getFirst16Bytes("ZhongHuaShiCi.com");

	// Don't change the initial vector:
	static final byte[] INIT_VECTOR = getFirst16Bytes("Welcome~To~ZhongHuaShiCi!");

	public static String encryptByAES(String dataToEncrypt) {
		return encryptByAES(DEFAULT_KEY, dataToEncrypt);
	}

	public static String encryptByAES(String key, String dataToEncrypt) {
		return encryptByAES(getFirst16Bytes(key), dataToEncrypt);
	}

	public static String encryptByAES(byte[] key, String dataToEncrypt) {
		byte[] encrypted = null;
		try {
			IvParameterSpec iv = new IvParameterSpec(INIT_VECTOR);
			SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");

			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
			cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
			encrypted = cipher.doFinal(stringToBytes(dataToEncrypt));
		} catch (GeneralSecurityException e) {
			throw new RuntimeException(e);
		}
		return Base64.getEncoder().withoutPadding().encodeToString(encrypted);
	}

	public static String decryptByAES(String dataToDecrypt) {
		return decryptByAES(DEFAULT_KEY, dataToDecrypt);
	}

	public static String decryptByAES(String key, String dataToDecrypt) {
		return decryptByAES(getFirst16Bytes(key), dataToDecrypt);
	}

	public static String decryptByAES(byte[] key, String dataToDecrypt) {
		byte[] decrypted = null;
		try {
			IvParameterSpec iv = new IvParameterSpec(INIT_VECTOR);
			SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");

			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
			cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
			decrypted = cipher.doFinal(Base64.getDecoder().decode(dataToDecrypt));
		} catch (GeneralSecurityException e) {
			throw new RuntimeException(e);
		}
		return new String(decrypted, StandardCharsets.UTF_8);
	}

	static byte[] getFirst16Bytes(String s) {
		MessageDigest md = HashUtil.getMessageDigest("sha-1");
		byte[] sha1 = md.digest(s.getBytes(StandardCharsets.UTF_8));
		byte[] data = new byte[16];
		for (int i = 0; i < data.length; i++) {
			data[i] = sha1[i];
		}
		return data;
	}

	static byte[] stringToBytes(String s) {
		return s.getBytes(StandardCharsets.UTF_8);
	}

	public static void main(String[] args) {
		String data = "The World Wide Web's markup language has always been HTML.";
		String encrypted = encryptByAES(data);
		System.out.println(encrypted);
		String decrypted = decryptByAES(encrypted);
		System.out.println(decrypted);
		System.out.println(data.equals(decrypted) ? "PASS" : "FAIL");
	}
}
