package com.itranswarp.shici.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class FileUtilTest {

	@Test
	public void testGetFileExt() {
		assertEquals("", FileUtil.getFileExt(null));
		assertEquals("", FileUtil.getFileExt(""));
		assertEquals("", FileUtil.getFileExt("aabbcc"));
		assertEquals(".txt", FileUtil.getFileExt("a.txt"));
		assertEquals(".txt", FileUtil.getFileExt("a b.TXT"));
		assertEquals(".txt", FileUtil.getFileExt(".tXt"));
		assertEquals(".html", FileUtil.getFileExt("a.txt.html"));
		assertEquals(".numbers", FileUtil.getFileExt("\u4e2d\u6587.numbers"));
		assertEquals("", FileUtil.getFileExt("ext file.ExtensionIsTooLong"));
	}

	@Test
	public void testGetMainFileName() {
		assertEquals("(unnamed)", FileUtil.getMainFileName(null));
		assertEquals("(unnamed)", FileUtil.getMainFileName(""));
		assertEquals("(unnamed)", FileUtil.getMainFileName("   "));
		assertEquals("(unnamed)", FileUtil.getMainFileName("  ."));
		assertEquals("Abc", FileUtil.getMainFileName("  Abc  "));
		assertEquals("Abc", FileUtil.getMainFileName("  Abc.txt "));
		assertEquals("Abc", FileUtil.getMainFileName("Abc.numbers "));
		assertEquals("Abc.numbers", FileUtil.getMainFileName(" Abc.numbers.txt "));
		assertEquals("\u4e2d\u6587", FileUtil.getMainFileName(" \u4e2d\u6587 .txt "));
		assertEquals("txt", FileUtil.getMainFileName(" txt. \u4e2d\u6587  "));
		assertEquals(
				"1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890",
				FileUtil.getMainFileName(
						"1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890"));
		assertEquals(
				"1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567...",
				FileUtil.getMainFileName(
						"1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890aaa"));
		assertEquals(
				"1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567...",
				FileUtil.getMainFileName(
						"1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890aaa.txt"));
	}
}
