package com.itranswarp.shici.search;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.itranswarp.shici.util.HttpUtil;
import com.itranswarp.shici.util.HttpUtil.HttpResponse;
import com.itranswarp.shici.util.JsonUtil;
import com.itranswarp.shici.util.MapUtil;

@Component
public class Searcher {

	final Log log = LogFactory.getLog(getClass());

	static final Map<String, String> JSON_HEADERS = MapUtil.createMap("Content-Type", "application/json");

	@Value("${es.url}")
	String esUrl;

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
		//
	}

	<T> T getJSON(Class<T> clazz, String path) {
		try {
			HttpResponse resp = HttpUtil.httpGet(esUrl + path, null, null);
			return checkResponse(resp, clazz);
		} catch (IOException e) {
			throw new SearchException(e);
		}
	}

	<T> T postJSON(Class<T> clazz, String path, Object data) {
		try {
			HttpResponse resp = HttpUtil.httpPost(esUrl + path, data == null ? null : JsonUtil.toJson(data),
					JSON_HEADERS);
			return checkResponse(resp, clazz);
		} catch (IOException e) {
			throw new SearchException(e);
		}
	}

	<T> T putJSON(Class<T> clazz, String path, Object data) {
		try {
			HttpResponse resp = HttpUtil.httpPut(esUrl + path, data == null ? null : JsonUtil.toJson(data),
					JSON_HEADERS);
			return checkResponse(resp, clazz);
		} catch (IOException e) {
			throw new SearchException(e);
		}
	}

	<T> T checkResponse(HttpResponse resp, Class<T> clazz) {
		if (resp.status == 200) {
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
}
