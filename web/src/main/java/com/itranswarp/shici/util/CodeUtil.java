package com.itranswarp.shici.util;

import java.util.UUID;

/**
 * Generate a 6-char String code used for referer code.
 * 
 * @author michael
 */
public class CodeUtil {

	static final char[] CHARS = "abcdefghijkmnpqrstuvwxyz23456789".toCharArray();

	/**
	 * Generate a 6-char String code.
	 * 
	 * @return 6-char String.
	 */
	public static String generate6CharsCode() {
		char[] cs = new char[6];
		int code = 0x3fffffff & UUID.randomUUID().toString().hashCode();
		cs[0] = CHARS[(code & 0x3e000000) >> 25];
		cs[1] = CHARS[(code & 0x1f00000) >> 20];
		cs[2] = CHARS[(code & 0xf8000) >> 15];
		cs[3] = CHARS[(code & 0x7c00) >> 10];
		cs[4] = CHARS[(code & 0x3e0) >> 5];
		cs[5] = CHARS[code & 0x1f];
		return new String(cs);
	}

}
