package com.itranswarp.shici.search;

import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.itranswarp.compiler.JavaStringCompiler;
import com.itranswarp.shici.searchable.SearchablePoem;

public class DocumentWrapperSourceBuilderTest {

	DocumentWrapperSourceBuilder builder;

	@Before
	public void setUp() throws Exception {
		builder = new DocumentWrapperSourceBuilder();
	}

	@Test
	public void testCreateSourceAndCompile() throws Exception {
		String source = builder.createSource(SearchablePoem.class);
		JavaStringCompiler compiler = new JavaStringCompiler();
		Map<String, byte[]> results = compiler.compile(SearchablePoem.class.getSimpleName() + "DocumentWrapper.java",
				source);
		assertEquals(1, results.size());
		Class<?> clazz = compiler.loadClass(SearchablePoem.class.getName() + "DocumentWrapper", results);
		@SuppressWarnings("unchecked")
		DocumentWrapper<SearchablePoem> dw = (DocumentWrapper<SearchablePoem>) clazz.newInstance();
		assertNotNull(dw);
	}

}
