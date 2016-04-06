package com.itranswarp.shici.search;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class SearcherTest {

	final String INDEX = "thetestindex";

	Searcher searcher;

	@Before
	public void setUp() throws Exception {
		searcher = new Searcher();
		searcher.esUrl = "http://localhost:9200/";
		if (!searcher.indexExist(INDEX)) {
			searcher.createIndex(INDEX);
		}
	}

	@Test
	public void testIndexExist() {
		assertFalse(searcher.indexExist("notexist"));
		assertTrue(searcher.indexExist(INDEX));
	}

}
