package com.itranswarp.shici.search;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.itranswarp.shici.compiler.StringCompiler;
import com.itranswarp.shici.util.HttpUtil;
import com.itranswarp.shici.util.HttpUtil.HttpResponse;
import com.itranswarp.shici.util.JsonUtil;
import com.itranswarp.shici.util.MapUtil;
import com.itranswarp.shici.util.ValidateUtil;
import com.itranswarp.warpdb.entity.BaseEntity;

@Component
public class Searcher {

	final Log log = LogFactory.getLog(getClass());

	static final Map<String, String> JSON_HEADERS = MapUtil.createMap("Content-Type", "application/json");

	@Value("${es.url}")
	String esUrl;

	// search /////////////////////////////////////////////////////////////////

	public <T extends BaseEntity> List<T> search(String indexName, Class<T> clazz, String[] qs, int pageIndex) {
		// search:
		List<String> searchableFields = this.getSearchableFields(clazz);
		// build query:
		String searchOptions = "{  }";
		postJSON(Map.class, indexName + "/_search", searchOptions);
		return null;
	}

	// document ///////////////////////////////////////////////////////////////

	public <T extends BaseEntity> void createMapping(String indexName, Class<T> clazz) {
		Map<String, Map<String, String>> properties = this.createMapping(clazz);
		putJSON(Map.class, indexName + "/_mapping/" + clazz.getSimpleName(),
				MapUtil.createMap("properties", properties));
	}

	public <T extends BaseEntity> void createDocument(String indexName, T doc) {
		ValidateUtil.checkId(doc.id);
		putJSON(Map.class, indexName + "/" + doc.getClass().getSimpleName() + "/" + doc.id, doc);
	}

	public <T extends BaseEntity> T getDocument(String indexName, Class<T> clazz, String id) {
		ValidateUtil.checkId(id);
		Class<? extends DocumentWrapper<T>> cls = getWrapperClass(clazz);
		log.info(cls);
		return getJSON(cls, indexName + "/" + clazz.getSimpleName() + "/" + id).getDocument();
	}

	public <T> void deleteDocument(String indexName, Class<T> clazz, String id) {
		ValidateUtil.checkId(id);
		deleteJSON(Map.class, indexName + "/" + clazz.getSimpleName() + "/" + id, null);
	}

	// index //////////////////////////////////////////////////////////////////

	public boolean indexExist(String name) {
		try {
			getJSON(Map.class, name);
		} catch (SearchResultException e) {
			return false;
		}
		return true;
	}

	public void createIndex(String name) {
		putJSON(Map.class, name, null);
	}

	public void deleteIndex(String name) {
		deleteJSON(Map.class, name, null);
	}

	// helper /////////////////////////////////////////////////////////////////

	Map<String, List<String>> searchableFieldsCache = new ConcurrentHashMap<String, List<String>>();

	List<String> getSearchableFields(Class<?> clazz) {
		List<String> list = searchableFieldsCache.get(clazz.getName());
		if (list == null) {
			list = new ArrayList<String>();
			for (Field f : clazz.getFields()) {
				if (f.isAnnotationPresent(Analyzed.class)) {
					list.add(f.getName());
				}
			}
			searchableFieldsCache.put(clazz.getName(), list);
		}
		return list;
	}

	Map<String, Map<String, String>> createMapping(Class<?> clazz) {
		log.info("Building mapping for class: " + clazz.getName());
		Map<String, Map<String, String>> properties = new HashMap<String, Map<String, String>>();
		for (Field f : clazz.getFields()) {
			if (f.isAnnotationPresent(Analyzed.class)) {
				properties.put(f.getName(), getMappingProperty(f.getType(), true));
			} else {
				Map<String, String> mapping = getMappingProperty(f.getType(), false);
				if (mapping != null) {
					properties.put(f.getName(), mapping);
				} else {
					log.info("Ignore unsupported field: " + f.getName());
				}
			}
		}
		return properties;
	}

	Map<String, String> getMappingProperty(Class<?> clazz, boolean analyzed) {
		String type;
		switch (clazz.getName()) {
		case "java.lang.String":
			type = "string";
			break;
		case "int":
		case "java.lang.Integer":
			type = "integer";
			break;
		case "long":
		case "java.lang.Long":
			type = "long";
			break;
		case "float":
		case "java.lang.Float":
			type = "float";
			break;
		case "double":
		case "java.lang.Double":
			type = "double";
			break;
		default:
			return null;
		}
		return MapUtil.createMap("type", type, "index", analyzed ? "analyzed" : "not_analyzed");
	}

	<T> T getJSON(Class<T> clazz, String path) {
		log.info("GET: " + esUrl + path);
		try {
			HttpResponse resp = HttpUtil.httpGet(esUrl + path, null, null);
			return checkResponse(resp, clazz);
		} catch (IOException e) {
			throw new SearchException(e);
		}
	}

	<T> T postJSON(Class<T> clazz, String path, Object data) {
		log.info("POST: " + esUrl + path);
		try {
			HttpResponse resp = HttpUtil.httpPost(esUrl + path, data == null ? null : JsonUtil.toJson(data),
					JSON_HEADERS);
			return checkResponse(resp, clazz);
		} catch (IOException e) {
			throw new SearchException(e);
		}
	}

	<T> T putJSON(Class<T> clazz, String path, Object data) {
		log.info("PUT: " + esUrl + path);
		try {
			HttpResponse resp = HttpUtil.httpPut(esUrl + path, data == null ? null : JsonUtil.toJson(data),
					JSON_HEADERS);
			return checkResponse(resp, clazz);
		} catch (IOException e) {
			throw new SearchException(e);
		}
	}

	<T> T deleteJSON(Class<T> clazz, String path, Object data) {
		log.info("DELETE: " + esUrl + path);
		try {
			HttpResponse resp = HttpUtil.httpDelete(esUrl + path, data == null ? null : JsonUtil.toJson(data),
					JSON_HEADERS);
			return checkResponse(resp, clazz);
		} catch (IOException e) {
			throw new SearchException(e);
		}
	}

	<T> T checkResponse(HttpResponse resp, Class<T> clazz) {
		if (resp.isOK()) {
			log.info("Response: " + resp.body);
			return JsonUtil.fromJson(clazz, resp.body);
		}
		log.info("Error Response: " + resp.body);
		String jsonErr = resp.body;
		if (jsonErr == null) {
			jsonErr = "{}";
		}
		throw JsonUtil.fromJson(SearchResultException.class, jsonErr);
	}

	Map<String, Class<? extends DocumentWrapper<?>>> cachedCompiledClasses = new ConcurrentHashMap<String, Class<? extends DocumentWrapper<?>>>();

	@SuppressWarnings("unchecked")
	<T extends BaseEntity> Class<? extends DocumentWrapper<T>> getWrapperClass(Class<T> clazz) {
		String T = clazz.getName();
		Class<? extends DocumentWrapper<T>> compiledClass = (Class<? extends DocumentWrapper<T>>) cachedCompiledClasses
				.get(T);
		if (compiledClass == null) {
			String packageName = clazz.getPackage().getName();
			String wrapperClassName = "Wrapper_" + clazz.getSimpleName();
			StringBuilder sb = new StringBuilder(256);
			sb.append("package " + packageName + ";\n");
			sb.append("public class " + wrapperClassName + " implements " + DocumentWrapper.class.getName() + "<" + T
					+ "> {\n");
			sb.append("    public String _id;\n");
			sb.append("    public " + T + " _source;\n");
			sb.append("    public " + T + " getDocument() {\n");
			sb.append("        return this._source;\n");
			sb.append("    }\n");
			sb.append("}\n");
			String sourceCode = sb.toString();
			log.info("Generate Java source:\n" + sourceCode);
			StringCompiler compiler = new StringCompiler();
			compiledClass = compiler.compile(packageName + "." + wrapperClassName, sourceCode);
			cachedCompiledClasses.put(clazz.getName(), compiledClass);
		}
		return compiledClass;
	}

}
