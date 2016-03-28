package com.itranswarp.shici.web.view;

import com.google.common.cache.CacheBuilder;
import com.mitchellbosecke.pebble.PebbleEngine;

public class PebbleEngineEx extends PebbleEngine {

	public void setTemplateCacheSize(int size) {
		this.setTemplateCache(CacheBuilder.newBuilder().maximumSize(size).build());
	}

	public void init() {
		this.addExtension(new CustomPebbleExtension());
	}

}
