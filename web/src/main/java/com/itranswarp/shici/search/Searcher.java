package com.itranswarp.shici.search;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.itranswarp.shici.util.HttpUtil;
import com.itranswarp.shici.util.HttpUtil.HttpResponse;
import com.itranswarp.shici.util.JsonUtil;
import com.itranswarp.shici.util.MapUtil;
import com.itranswarp.shici.util.ValidateUtil;

@Component
public class Searcher {

	final Log log = LogFactory.getLog(getClass());

	static final Map<String, String> JSON_HEADERS = MapUtil.createMap("Content-Type", "application/json");

	@Value("${es.url}")
	String esUrl;

	// search /////////////////////////////////////////////////////////////////

	public <T extends Searchable> List<T> search(String indexName, Class<? extends HitsResultWrapper<T>> clazz,
			String[] qs, int maxResults) {
		// build query:
		Map<String, Object> query = buildQuery(qs);
		query.put("from", 0);
		query.put("size", maxResults);
		log.info("Query: " + JsonUtil.toJson(query));
		double minScore = 0.0;
		ParameterizedType t = (ParameterizedType) clazz.getGenericInterfaces()[0];
		Class<?> cls = (Class<?>) t.getActualTypeArguments()[0];
		HitsResultWrapper<T> hitsResultWrapper = postJSON(clazz, indexName + "/" + cls.getSimpleName() + "/_search",
				query);
		HitsWrapper<T> hitsWrapper = hitsResultWrapper.getHitsWrapper();
		int total = hitsWrapper.getTotal();
		if (total == 0) {
			return Collections.emptyList();
		}
		List<? extends DocumentWrapper<T>> list = hitsWrapper.getDocumentWrappers();
		List<T> results = new ArrayList<T>(list.size());
		for (DocumentWrapper<T> dw : list) {
			if (dw.getScore() > minScore) {
				results.add(dw.getDocument());
			}
		}
		return results;
	}

	Map<String, Object> buildQuery(String[] qs) {
		if (qs.length == 1) {
			return buildSingleQuery(qs[0]);
		}
		return buildBoolShouldQuery(qs);
	}

	// build: { "query": { "bool": { "should": [ ... ] } } }
	Map<String, Object> buildBoolShouldQuery(String[] qs) {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		int len = qs.length;
		if (len > 3) {
			len = 3;
		}
		for (int i = 0; i < len; i++) {
			list.add(buildSubQuery(qs[i]));
		}
		return MapUtil.createMap("query", MapUtil.createMap("bool", MapUtil.createMap("should", list)));
	}

	Map<String, Object> buildSubQuery(String q) {
		if (q.length() == 1) {
			return buildTermQuery(q);
		}
		if (q.length() <= 3) {
			return buildPhraseQuery(q);
		}
		return buildFuzzyPhraseQuery(q);
	}

	/**
	 * "bool": { "should": [ { "match_phrase" : { "_all" : { "query": "日照香炉升紫烟",
	 * "slop": 1 } } }, { "multi_match": { "fields": ["title", "message"],
	 * "query": "日照香炉升紫烟", "minimum_should_match": "60%", "fuzziness": 0 } } ] }
	 */
	Map<String, Object> buildFuzzyPhraseQuery(String q) {
		if (q.length() > 7) {
			q = q.substring(0, 7);
		}
		List<Map<String, Object>> shoulds = new ArrayList<Map<String, Object>>(2);
		shoulds.add(buildPhraseQuery(q));
		shoulds.add(MapUtil.createMap("multi_match",
				MapUtil.createMap("fields", "_all", "query", q, "minimum_should_match", "75%")));
		return MapUtil.createMap("bool", MapUtil.createMap("should", shoulds));
	}

	// build: { "match_phrase": { "_all": { "query": "xxx", "slop": 1 } } }
	Map<String, Object> buildPhraseQuery(String q) {
		return MapUtil.createMap("match_phrase", MapUtil.createMap("_all", MapUtil.createMap("query", q, "slop", 1)));
	}

	// build: { "query": {...} }
	Map<String, Object> buildSingleQuery(String q) {
		if (q.length() == 1) {
			return MapUtil.createMap("query", buildTermQuery(q));
		}
		if (q.length() <= 3) {
			return MapUtil.createMap("query", buildPhraseQuery(q));
		}
		return MapUtil.createMap("query", buildFuzzyPhraseQuery(q));
	}

	// build: { "term": {...} }
	Map<String, Object> buildTermQuery(String term) {
		return MapUtil.createMap("term", MapUtil.createMap("_all", MapUtil.createMap("value", term, "boost", 0.12)));
	}

	// document ///////////////////////////////////////////////////////////////

	public <T extends Searchable> void createMapping(String indexName, Class<T> clazz) {
		Map<String, Map<String, String>> properties = this.createMapping(clazz);
		putJSON(Map.class, indexName + "/_mapping/" + clazz.getSimpleName(),
				MapUtil.createMap("properties", properties));
	}

	public <T extends Searchable> void createDocument(String indexName, T doc) {
		ValidateUtil.checkId(doc.getId());
		putJSON(Map.class, indexName + "/" + doc.getClass().getSimpleName() + "/" + doc.getId(), doc);
	}

	public <T extends Searchable> T getDocument(String indexName, Class<? extends DocumentWrapper<T>> clazz,
			String id) {
		ValidateUtil.checkId(id);
		DocumentWrapper<T> wrapper = getJSON(clazz, indexName + "/" + clazz.getSimpleName() + "/" + id);
		return wrapper.getDocument();
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

	// Map<String, Class<?>> cachedHitsWrapperClasses = new
	// ConcurrentHashMap<String, Class<?>>();
	//
	// @SuppressWarnings("unchecked")
	// <T extends Searchable> Class<? extends HitsWrapper<T>>
	// getHitsWrapperClass(Class<T> clazz) {
	// getDocumentWrapperClass(clazz);
	// String T = clazz.getName();
	// Class<? extends HitsWrapper<T>> compiledClass = (Class<? extends
	// HitsWrapper<T>>) cachedHitsWrapperClasses
	// .get(T);
	// if (compiledClass == null) {
	// Class<?> hitsHits = compileHits(clazz);
	// compiledClass = (Class<? extends HitsWrapper<T>>)
	// compileHitsResult(clazz, hitsHits);
	// cachedHitsWrapperClasses.put(clazz.getName(), compiledClass);
	// }
	// return compiledClass;
	// }
	//
	// <T extends Searchable> Class<?> compileHitsResult(Class<T> clazz,
	// Class<?> hits) {
	// String T = clazz.getName();
	// String packageName = clazz.getPackage().getName();
	// String hitsClassName = "HitsResult_" + clazz.getSimpleName();
	// StringBuilder sb = new StringBuilder(256);
	// sb.append("package " + packageName + ";\n");
	// sb.append("public class " + hitsClassName + " implements " +
	// HitsResultWrapper.class.getName() + "<" + T
	// + "> {\n");
	// sb.append(" public int took;\n");
	// sb.append(" public " + hits.getName() + " hits;\n");
	// sb.append(" public " + HitsWrapper.class.getName() + "<" + T + ">
	// getHitsWrapper() {\n");
	// sb.append(" return this.hits;\n");
	// sb.append(" }\n");
	// sb.append("}\n");
	// String sourceCode = sb.toString();
	// log.info("Generate Java source: " + hitsClassName + ".java\n" +
	// sourceCode);
	// StringCompiler compiler = new StringCompiler();
	// return compiler.compile(packageName + "." + hitsClassName, sourceCode);
	// }
	//
	// <T extends Searchable> Class<?> compileHits(Class<T> clazz) {
	// String T = clazz.getName();
	// String packageName = clazz.getPackage().getName();
	// String hitsClassName = "Hits__" + clazz.getSimpleName();
	// StringBuilder sb = new StringBuilder(256);
	// sb.append("package " + packageName + ";\n");
	// sb.append("public class " + hitsClassName + " implements " +
	// HitsWrapper.class.getName() + "<" + T + "> {\n");
	// sb.append(" public int total;\n");
	// sb.append(" public int getTotal() {\n");
	// sb.append(" return this.total;\n");
	// sb.append(" }\n");
	// sb.append(" public java.util.List<" + DocumentWrapper.class.getName() +
	// "<" + T + ">> hits;\n");
	// sb.append(" public java.util.List<" + DocumentWrapper.class.getName() +
	// "<" + T
	// + ">> getDocumentWrappers() {\n");
	// sb.append(" return this.hits;\n");
	// sb.append(" }\n");
	// sb.append("}\n");
	// String sourceCode = sb.toString();
	// log.info("Generate Java source: " + hitsClassName + ".java\n" +
	// sourceCode);
	// StringCompiler compiler = new StringCompiler();
	// return compiler.compile(packageName + "." + hitsClassName, sourceCode);
	// }
	//
	// Map<String, Class<?>> cachedCompiledWrapperClasses = new
	// ConcurrentHashMap<String, Class<?>>();
	//
	// @SuppressWarnings("unchecked")
	// <T extends Searchable> Class<? extends DocumentWrapper<T>>
	// getDocumentWrapperClass(Class<T> clazz) {
	// String T = clazz.getName();
	// Class<? extends DocumentWrapper<T>> compiledClass = (Class<? extends
	// DocumentWrapper<T>>) cachedCompiledWrapperClasses
	// .get(T);
	// if (compiledClass == null) {
	// compiledClass = compileDocumentWrapperClass(clazz);
	// cachedCompiledWrapperClasses.put(clazz.getName(), compiledClass);
	// }
	// return compiledClass;
	// }
	//
	// <T extends Searchable> Class<? extends DocumentWrapper<T>>
	// compileDocumentWrapperClass(Class<T> clazz) {
	// String T = clazz.getName();
	// String packageName = clazz.getPackage().getName();
	// String mainClassName = "DocumentWrapper__" + clazz.getSimpleName();
	// StringBuilder sb = new StringBuilder(256);
	// sb.append("package " + packageName + ";\n");
	// sb.append(
	// "public class " + mainClassName + " implements " +
	// DocumentWrapper.class.getName() + "<" + T + "> {\n");
	// sb.append(" public String _id;\n");
	// sb.append(" public double _score;\n");
	// sb.append(" public double getScore() {\n");
	// sb.append(" return this._score;\n");
	// sb.append(" }\n");
	// sb.append(" public " + T + " _source;\n");
	// sb.append(" public " + T + " getDocument() {\n");
	// sb.append(" return this._source;\n");
	// sb.append(" }\n");
	// sb.append("}\n");
	// String sourceCode = sb.toString();
	// log.info("Generate Java source: " + mainClassName + ".java\n" +
	// sourceCode);
	// StringCompiler compiler = new StringCompiler();
	// return compiler.compile(packageName + "." + mainClassName, sourceCode);
	// }
}
