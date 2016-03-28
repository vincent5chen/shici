package com.itranswarp.shici.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class Base64Util {

	public static String encodeToString(String s) {
		return encodeToString(s.getBytes(StandardCharsets.UTF_8));
	}

	public static String encodeToString(byte[] data) {
		return Base64.getEncoder().encodeToString(data);
	}

	public static String decodeToString(String s) {
		return new String(decode(s), StandardCharsets.UTF_8);
	}

	public static byte[] decode(String s) {
		return Base64.getDecoder().decode(s);
	}

	public static String urlEncodeToString(String s) {
		return Base64.getUrlEncoder().withoutPadding().encodeToString(s.getBytes(StandardCharsets.UTF_8));
	}

	public static String urlDecodeToString(String s) {
		return new String(Base64.getUrlDecoder().decode(s), StandardCharsets.UTF_8);
	}
}
