package com.itranswarp.shici.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class EncryptUtilTest {

	@Test
	public void testEncyptDecrypt() {
		String[] sources = { "", "hahaha", "hehe:) hehe", "English&\n\u4e2d\u6587\t\n", "\u0000\uffff" };
		for (String source : sources) {
			String encrypted = EncryptUtil.encryptByAES(source);
			assertEquals(source, EncryptUtil.decryptByAES(encrypted));
		}
	}

}
