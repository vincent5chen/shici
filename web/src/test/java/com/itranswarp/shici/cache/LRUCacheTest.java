package com.itranswarp.shici.cache;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class LRUCacheTest {
	LRUCache cache;

	@Before
	public void setUp() {
		cache = new LRUCache(5);
	}

	@Test
	public void testGetSet() {
		cache.set("key1", "value1");
		cache.set("key2", "value2");
		assertEquals("value1", cache.get("key1"));
		assertEquals("value2", cache.get("key2"));
		assertNull(cache.get("key3"));
		cache.set("key3", "value3");
		assertEquals("value3", cache.get("key3"));
	}

	@Test
	public void testTooManyItems() {
		cache.set("key1", "value1");
		cache.set("key2", "value2");
		cache.set("key3", "value3");
		cache.set("key4", "value4");
		cache.set("key5", "value5");
		cache.set("key6", "value6");
		assertNull(cache.get("key1"));
		cache.set("key7", "value6");
		// key2 was removed for cache out of size:
		assertNull(cache.get("key2"));
	}

	@Test
	public void testLRU() {
		cache.set("key1", "value1");
		cache.set("key2", "value2");
		cache.set("key3", "value3");
		cache.set("key4", "value4");
		cache.set("key5", "value5");
		cache.get("key1");
		cache.set("key6", "value6");
		assertNull(cache.get("key2"));
		cache.set("key7", "value6");
		// key3 was removed for cache out of size:
		assertNull(cache.get("key3"));
	}

	@Test
	public void testSetExpires() throws Exception {
		assertNull(cache.get("k"));
		cache.set("k", "v", 1);
		assertEquals("v", cache.get("k"));
		Thread.sleep(1100);
		assertNull(cache.get("k"));
	}

}

@FunctionalInterface
interface Getter<K, V> {
	V getByKey(K key);
}
