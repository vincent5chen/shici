package com.itranswarp.shici.cache;

import java.io.IOException;
import java.util.function.Function;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.spy.memcached.AddrUtil;
import net.spy.memcached.BinaryConnectionFactory;
import net.spy.memcached.MemcachedClient;

/**
 * A memcached based caching service.
 */
public class MemCache implements Cache {

	final Log log = LogFactory.getLog(getClass());

	final String servers;
	final int expires;

	MemcachedClient mc = null;

	public MemCache(String servers, int expires) {
		this.servers = servers == null || servers.isEmpty() ? "127.0.0.1:11211" : servers;
		this.expires = expires;
		try {
			this.mc = new MemcachedClient(new BinaryConnectionFactory(), AddrUtil.getAddresses(this.servers));
		} catch (IOException e) {
			log.warn("Cannot connect to memcached. Cache is disabled.");
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T get(String key) {
		if (this.mc == null) {
			return null;
		}
		return (T) this.mc.get(key);
	}

	@Override
	public <T> T get(String key, Function<String, T> fn) {
		if (this.mc == null) {
			return fn.apply(key);
		}
		T t = get(key);
		if (t == null) {
			log.info("Key is missing in memcached: " + key);
			t = fn.apply(key);
			this.mc.set(key, this.expires, t);
			log.info("Put object to memcached: " + key);
		} else {
			log.info("Key hit in memcached: " + key);
		}
		return t;
	}

	@Override
	public void remove(String key) {
		if (this.mc != null) {
			log.info("Manualy remove key from memcached: " + key);
			this.mc.delete(key);
		}
	}

	@Override
	public <T> void set(String key, T t) {
		set(key, t, expires);
	}

	@Override
	public <T> void set(String key, T t, int seconds) {
		// TODO Auto-generated method stub

	}
}
