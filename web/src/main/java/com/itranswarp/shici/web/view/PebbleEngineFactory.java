package com.itranswarp.shici.web.view;

import javax.servlet.ServletContext;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.web.context.ServletContextAware;

import com.google.common.cache.CacheBuilder;
import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.loader.ServletLoader;

public class PebbleEngineFactory implements FactoryBean<PebbleEngine>, ServletContextAware {

	int templateCacheSize = 0;
	ServletContext servletContext;

	public void setTemplateCacheSize(int size) {
		this.templateCacheSize = size;
	}

	@Override
	public PebbleEngine getObject() throws Exception {
		// TODO Auto-generated method stub
		return new PebbleEngine.Builder().autoEscaping(true).loader(new ServletLoader(servletContext))
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

	@Override
	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}
}
