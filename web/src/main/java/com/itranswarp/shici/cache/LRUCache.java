package com.itranswarp.shici.cache;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class LRUCache implements Cache {

	final static int DEFAULT_TTL = 60;

	final int size;
	final ReadWriteLock rwLock = new ReentrantReadWriteLock();
	final Lock rLock = rwLock.readLock();
	final Lock wLock = rwLock.writeLock();

	Map<String, CacheItem> cache;

	public LRUCache() {
		this(32);
	}

	public LRUCache(int size) {
		this.size = size;
		this.cache = new LinkedHashMap<String, CacheItem>() {
			protected boolean removeEldestEntry(Map.Entry<String, CacheItem> eldest) {
				System.out.println("size()=" + size());
				return size() >= size;
			}
		};
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T get(String key) {
		CacheItem ci = null;
		rLock.lock();
		try {
			ci = cache.get(key);
		} finally {
			rLock.unlock();
		}
		if (ci != null && ci.isExpired()) {
			remove(key);
			return null;
		}
		return ci == null ? null : (T) ci.value;
	}

	@Override
	public <T> void set(String key, T t) {
		set(key, t, DEFAULT_TTL);
	}

	@Override
	public <T> void set(String key, T t, int seconds) {
		wLock.lock();
		try {
			cache.put(key, new CacheItem(t, seconds));
		} finally {
			wLock.unlock();
		}
	}

	@Override
	public void remove(String key) {
		wLock.lock();
		try {
			cache.remove(key);
		} finally {
			wLock.unlock();
		}
	}
}

class CacheItem {

	public CacheItem(Object value, int seconds) {
		this.value = value;
		this.expiresAt = System.currentTimeMillis() + 1000L * seconds;
	}

	final Object value;
	long expiresAt;

	boolean isExpired() {
		return expiresAt < System.currentTimeMillis();
	}
}
