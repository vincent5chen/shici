package com.itranswarp.shici.search;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

abstract class SourceBuilder {

	Log log = LogFactory.getLog(getClass());

	protected abstract String getTemplate();

	public abstract String getClassName(Class<?> clazz);

	public abstract String getFileName(Class<?> clazz);

	public String createSource(Class<?> clazz) {
		String className = clazz.getSimpleName();
		String packageName = clazz.getPackage().getName();
		String source = getTemplate().replace("${package}", packageName).replace("${name}", className);
		log.info("Generate source:\n" + source);
		return source;
	}
}
