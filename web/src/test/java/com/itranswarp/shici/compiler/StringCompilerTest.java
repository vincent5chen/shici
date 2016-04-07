package com.itranswarp.shici.compiler;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class StringCompilerTest {

	StringCompiler compiler;

	@Before
	public void setUp() throws Exception {
		compiler = new StringCompiler();
	}

	@Test
	public void testCompile() throws Exception {
		StringBuilder sb = new StringBuilder(1024);
		sb.append("package on.the.fly;                                           \n");
		sb.append("public class Flying implements java.util.Comparator<String> { \n");
		sb.append("    public int height() {                                     \n");
		sb.append("        return 8080;                                          \n");
		sb.append("    }                                                         \n");
		sb.append("    @Override                                                 \n");
		sb.append("    public int compare(String o1, String o2) {                \n");
		sb.append("        return o1.toLowerCase().compareTo(o2.toLowerCase());  \n");
		sb.append("    }                                                         \n");
		sb.append("}                                                             \n");
		String source = sb.toString();
		Class<?> clazz = compiler.compile("on.the.fly.Flying", source);
		assertEquals("on.the.fly.Flying", clazz.getName());
		assertEquals(8080, clazz.getMethod("height").invoke(clazz.newInstance()));
	}

}
