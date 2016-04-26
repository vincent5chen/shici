package com.itranswarp.shici.search;

import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.itranswarp.compiler.JavaStringCompiler;
import com.itranswarp.shici.searchable.SearchablePoem;

public class HitsResultWrapperSourceBuilderTest {

	HitsResultWrapperSourceBuilder builder;

	@Before
	public void setUp() throws Exception {
		builder = new HitsResultWrapperSourceBuilder();
	}

	@Test
	public void testCreateSourceAndCompile() throws Exception {
		String source = builder.createSource(SearchablePoem.class);
		JavaStringCompiler compiler = new JavaStringCompiler();
		Map<String, byte[]> results = compiler.compile(SearchablePoem.class.getSimpleName() + "HitsResultWrapper.java",
				source);
		assertEquals(3, results.size());
		Class<?> clazz = compiler.loadClass(SearchablePoem.class.getName() + "HitsResultWrapper", results);
		@SuppressWarnings("unchecked")
		HitsResultWrapper<SearchablePoem> dw = (HitsResultWrapper<SearchablePoem>) clazz.newInstance();
		assertNotNull(dw);
	}

}
