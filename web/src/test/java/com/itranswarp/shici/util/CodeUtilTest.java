package com.itranswarp.shici.util;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.junit.Test;

public class CodeUtilTest {

	@Test
	public void testGenerate6CharsCode() {
		Pattern p = Pattern.compile("^[a-z0-9]{6}$");
		Set<String> set = new HashSet<String>();
		for (int i = 0; i < 1000; i++) {
			String code = CodeUtil.generate6CharsCode();
			assertTrue(p.matcher(code).matches());
			set.add(code);
		}
		assertTrue(set.size() >= 990);
	}

}
