package com.itranswarp.shici.web.view;

import org.springframework.beans.factory.FactoryBean;

import com.google.common.cache.CacheBuilder;
import com.mitchellbosecke.pebble.PebbleEngine;

public class PebbleEngineFactory implements FactoryBean<PebbleEngine> {

	int templateCacheSize = 0;

	public void setTemplateCacheSize(int size) {
		this.templateCacheSize = size;
	}

	@Override
	public PebbleEngine getObject() throws Exception {
		// TODO Auto-generated method stub
		return new PebbleEngine.Builder().autoEscaping(true)
				.templateCache(CacheBuilder.newBuilder().maximumSize(templateCacheSize).build())
				.extension(new CustomPebbleExtension()).build();
	}

	@Override
	public Class<PebbleEngine> getObjectType() {
		return PebbleEngine.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}
}
