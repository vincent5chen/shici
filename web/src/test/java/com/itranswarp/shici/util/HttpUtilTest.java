package com.itranswarp.shici.util;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class HttpUtilTest {

	@Test
	public void testGuessContentType() {
		assertEquals("text/html", HttpUtil.guessContentType("aa.html"));
		assertEquals("text/html", HttpUtil.guessContentType(".html"));
		assertEquals("image/jpeg", HttpUtil.guessContentType("a b.JPG"));
		assertEquals("application/octet-stream", HttpUtil.guessContentType(""));
		assertEquals("application/octet-stream", HttpUtil.guessContentType("html"));
		assertEquals("application/octet-stream", HttpUtil.guessContentType("aa.unknowntype"));
	}

	@Test
	public void testUrlEncode() {
		assertEquals("Hello%2C+world%21", HttpUtil.urlEncode("Hello, world!"));
		assertEquals("Hello+%26+world", HttpUtil.urlEncode("Hello & world"));
		assertEquals("Hello+%3D+world%21", HttpUtil.urlEncode("Hello = world!"));
		assertEquals("Hello%2C+%E4%B8%96%E7%95%8C", HttpUtil.urlEncode("Hello, \u4e16\u754c"));
	}

	@Test
	public void testUrlDecode() {
		assertEquals("Hello, world!", HttpUtil.urlDecode("Hello%2C+world%21"));
		assertEquals("Hello & world", HttpUtil.urlDecode("Hello+%26+world"));
		assertEquals("Hello = world!", HttpUtil.urlDecode("Hello+%3D+world%21"));
		assertEquals("Hello, \u4e16\u754c", HttpUtil.urlDecode("Hello%2C+%E4%B8%96%E7%95%8C"));
	}

	@Test
	public void testUrlDecodeAsMap() {
		Map<String, String> qs = HttpUtil
				.urlDecodeAsMap("q=%E4%B8%96%E7%95%8C&source=lnms&invalid&tbm=nws&sa=&biw=1361&end");
		assertEquals("\u4e16\u754c", qs.get("q"));
		assertEquals("lnms", qs.get("source"));
		assertEquals("nws", qs.get("tbm"));
		assertEquals("", qs.get("sa"));
		assertEquals("1361", qs.get("biw"));
		assertFalse(qs.containsKey("invalid"));
	}

	@Test
	public void testHttpGetOK() throws IOException {
		Map<String, String> data = new HashMap<String, String>() {
			{
				put("ref", "test");
			}
		};
		String r = HttpUtil.httpGet("http://www.liaoxuefeng.com/", data, null);
		assertTrue(r.contains("\u5ed6\u96ea\u5cf0"));
	}

}
