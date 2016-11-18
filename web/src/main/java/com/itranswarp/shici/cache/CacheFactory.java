package com.itranswarp.shici.cache;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CacheFactory implements FactoryBean<Cache> {

	@Value("${cache.type:}")
	String cacheType;

	@Value("${cache.servers:}")
	String cacheServers;

	@Value("${cache.expires:3600}")
	int cacheExpires;

	@Override
	public Cache getObject() throws Exception {
		switch (this.cacheType) {
		case "":
			return new LRUCache();
		case "memcached":
			return new MemCache(cacheServers, cacheExpires);
		default:
			throw new IllegalArgumentException("Invalid property: cache.type=" + this.cacheType);
		}
	}

	@Override
	public Class<?> getObjectType() {
		return Cache.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

}
