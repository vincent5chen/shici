package com.itranswarp.shici.search;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.itranswarp.warpdb.IdUtil;
import com.itranswarp.warpdb.entity.BaseEntity;

public class SearcherTest {

	final String INDEX = "thetestindex";

	Searcher searcher;

	@Before
	public void setUp() throws Exception {
		searcher = new Searcher();
		searcher.esUrl = "http://localhost:9200/";
		if (searcher.indexExist(INDEX)) {
			searcher.deleteIndex(INDEX);
		}
		searcher.createIndex(INDEX);
	}

	@Test
	public void testIndexExist() {
		assertFalse(searcher.indexExist("notexist"));
		assertTrue(searcher.indexExist(INDEX));
	}

	@Test
	public void testCreateAndDeleteIndex() {
		String TEMP_NAME = "thetmpindex";
		searcher.createIndex(TEMP_NAME);
		assertTrue(searcher.indexExist(TEMP_NAME));
		searcher.deleteIndex(TEMP_NAME);
		assertFalse(searcher.indexExist(TEMP_NAME));
	}

	@Test
	public void testCreateDocAndGet() {
		Tweet t = Tweet.newTweet("michael", "Hello, from 北京奥林匹克森林公园！");
		searcher.createDocument(INDEX, t);
		// test
		Tweet gt = searcher.getDocument(INDEX, Tweet.class, t.id);
	}

}
