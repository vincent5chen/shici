package com.itranswarp.shici.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class Base64UtilTest {

	@Test
	public void testEncode() {
		assertEquals("", Base64Util.encodeToString(""));
		assertEquals("77+/YWJjZA==", Base64Util.encodeToString("\uffffabcd"));
		assertEquals("YWJjIHh5eg==", Base64Util.encodeToString("abc xyz"));
		assertEquals("77+/AC3kuK3mloc=", Base64Util.encodeToString("\uffff\u0000-\u4e2d\u6587"));
	}

	@Test
	public void testDecode() {
		assertEquals("", Base64Util.decodeToString(""));
		assertEquals("\uffffabcd", Base64Util.decodeToString("77+/YWJjZA=="));
		assertEquals("abc xyz", Base64Util.decodeToString("YWJjIHh5eg=="));
		assertEquals("\u0000\u4e2d\u6587\u5b57\u7b26\u4e32\u7f16\u7801", Base64Util.decodeToString("AOS4reaWh+Wtl+espuS4sue8lueggQ=="));
	}

	@Test
	public void testUrlsafeEncode() {
		assertEquals("", Base64Util.urlEncodeToString(""));
		assertEquals("77-_YWJjZA", Base64Util.urlEncodeToString("\uffffabcd"));
		assertEquals("77-_AC3kuK3mloc", Base64Util.urlEncodeToString("\uffff\u0000-\u4e2d\u6587"));
	}

	@Test
	public void testUrlsafeDecode() {
		assertEquals("", Base64Util.urlDecodeToString(""));
		assertEquals("\uffffabcd", Base64Util.urlDecodeToString("77-_YWJjZA"));
		assertEquals("\uffff\u0000-\u4e2d\u6587", Base64Util.urlDecodeToString("77-_AC3kuK3mloc"));
	}

}
