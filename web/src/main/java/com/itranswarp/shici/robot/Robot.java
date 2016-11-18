package com.itranswarp.shici.robot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.itranswarp.wxapi.util.HttpUtil;
import com.itranswarp.wxapi.util.JsonUtil;
import com.itranswarp.wxapi.util.MapUtil;

@Component
public class Robot {

	static final String TULING_URL = "http://www.tuling123.com/openapi/api";

	@Value("${robot.app.ids}")
	String apiIds;

	String[] apiKeys;
	String[] apiSecrets;

	@PostConstruct
	public void init() {
		String[] ids = apiIds.trim().split(",");
		List<String> keys = new ArrayList<>();
		for (String id : ids) {
			String s = id.trim();
			if (!s.isEmpty() && !keys.contains(s)) {
				keys.add(s);
			}
		}
		apiKeys = keys.toArray(new String[keys.size()]);
	}

	public RobotResponse talk(String userId, String text) throws Exception {
		return talk(userId, text, null);
	}

	public RobotResponse talk(String userId, String text, String location) throws Exception {
		int n = selectKey(userId);
		String key = this.apiKeys[n];
		String secret = this.apiSecrets[n];
		Map<String, String> params = MapUtil.createMap("key", key, "userid", userId, "info", text);
		if (location != null && !location.isEmpty()) {
			params.put("loc", location);
		}
		String json = HttpUtil.httpPost(TULING_URL, HttpUtil.urlEncode(params), null);
		RobotResponse resp = JsonUtil.fromJson(RobotResponse.class, json);
		if (resp.code >= 40000 && resp.code <= 49999) {
			throw new RobotException(resp.code, resp.text);
		}
		return resp;
	}

	int selectKey(String userId) {
		return userId.hashCode() % this.apiKeys.length;
	}

	public static class RobotResponse {

		public static int CODE_TEXT = 100000;
		public static int CODE_LINK = 200000;
		public static int CODE_NEWS = 302000;
		public static int CODE_COOK = 308000;
		public static int CODE_SONG = 313000;
		public static int CODE_POEM = 314000;

		public int code;
		public String text;
		public String url;
		public Item[] list;
		public Func function;
	}

	public static class Item {
		public String article;
		public String source;
		public String icon;
		public String info;
		public String detailurl;
	}

	public static class Func {
		public String song;
		public String singer;
		public String author;
		public String name;
	}

}
