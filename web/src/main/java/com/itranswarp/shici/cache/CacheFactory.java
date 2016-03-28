package com.itranswarp.shici.cache;

import java.util.function.Function;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CacheFactory implements FactoryBean<Cache> {

	@Value("${cache.type:null}")
	String cacheType;

	@Value("${cache.servers:}")
	String cacheServers;

	@Value("${cache.expires:3600}")
	int cacheExpires;

	@Override
	public Cache getObject() throws Exception {
		switch (this.cacheType) {
		case "null":
			return new NullCache();
		case "memcached":
			return new MemCache(cacheServers, cacheExpires);
		}
		throw new IllegalArgumentException("Invalid property: cache.type=" + this.cacheType);
	}

	@Override
	public Class<?> getObjectType() {
		return Cache.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	static class NullCache implements Cache {

		@Override
		public <T> T get(String key) {
			return null;
		}

		@Override
		public <T> void set(String key, T t) {
		}

		@Override
		public <T> void set(String key, T t, int seconds) {
		}

		@Override
		public <T> T get(String key, Function<String, T> fn) {
			return fn.apply(key);
		}

		@Override
		public void remove(String key) {
		}

	}

}
