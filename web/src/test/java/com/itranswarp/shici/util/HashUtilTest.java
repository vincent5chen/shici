package com.itranswarp.shici.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class HashUtilTest {

	@Test
	public void testMd5() {
		assertEquals("708171654200ecd0e973167d8826159c", HashUtil.md5("hello, world."));
		assertEquals("25d55ad283aa400af464c76d713c07ad", HashUtil.md5("12345678"));
		assertEquals("4839047bda3e033478308e30b001848a", HashUtil.md5("www.w3.org"));
		assertEquals("c25cc7577d1f4fbb821582d207cb94e5", HashUtil.md5("Copyright &copy; 2015"));
	}

	@Test
	public void testSha1() {
		assertEquals("a1a6bb411bac18fc1d0c88eba5a841d0498ea6cf", HashUtil.sha1("hello, world."));
		assertEquals("7c222fb2927d828af22f592134e8932480637c0d", HashUtil.sha1("12345678"));
		assertEquals("f75f2fd284e7ab54005b787c137971d902ffb269", HashUtil.sha1("www.w3.org"));
		assertEquals("5b76f9c47d2ff7b38793bc9b953d36d1f3f56c37", HashUtil.sha1("Copyright &copy; 2015"));
	}

	@Test
	public void testSha256() {
		assertEquals("df4f2d5707fd85996a13aab607eb3f6b19d0e99d97281d5eae68a6c16317a329",
				HashUtil.sha256("hello, world."));
		assertEquals("ef797c8118f02dfb649607dd5d3f8c7623048c9c063d532cc95c5ed7a898a64f", HashUtil.sha256("12345678"));
		assertEquals("1a2e36cc692d119df0a1849e97aafb00564af3f638bb5fbe4c63be1cb6b82319", HashUtil.sha256("www.w3.org"));
		assertEquals("3cd35b6ad893974cfec7ed8201fecf2b51e237d83ef46247fd91433e94279e63",
				HashUtil.sha256("Copyright &copy; 2015"));
	}

	@Test
	public void testHmacSha1() {
		assertEquals("oo3EiqiGHfSnnJv20Hn1Ya+Ty9c=", HashUtil.hmacSha1("Hello, world.", "abc"));
		assertEquals("PtGBLUZsjCHYU/T05K7TxGgJkM0=", HashUtil.hmacSha1("Hello, world.", "xyz"));
	}
}
