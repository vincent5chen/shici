package com.itranswarp.shici.oauth;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.itranswarp.shici.util.HttpUtil;
import com.itranswarp.shici.util.JsonUtil;

@Component
public class QQOAuthProvider implements OAuthProvider {

	final Log log = LogFactory.getLog(getClass());

	@Value("${oauth.qq.appkey}")
	String appKey;

	@Value("${oauth.qq.appsecret}")
	String appSecret;

	@Override
	public String getName() {
		return "qq";
	}

	@Override
	public String getAuthenticateURL(String redirectUri) {
		StringBuilder sb = new StringBuilder(128);
		sb.append("https://graph.qq.com/oauth2.0/authorize?client_id=").append(appKey)
				.append("&response_type=code&state=").append(UUID.randomUUID().toString()).append("&redirect_uri=")
				.append(HttpUtil.urlEncode(redirectUri));
		return sb.toString();
	}

	@Override
	public OAuthAuthentication getAuthentication(String redirectUri, String code) throws IOException {
		Map<String, String> params = new HashMap<String, String>();
		params.put("client_id", this.appKey);
		params.put("client_secret", this.appSecret);
		params.put("grant_type", "authorization_code");
		params.put("redirect_uri", redirectUri);
		params.put("code", code);
		String resp = HttpUtil.httpGet("https://graph.qq.com/oauth2.0/token", params, null);
		log.info("Response: " + resp);
		Map<String, String> r = HttpUtil.urlDecodeAsMap(resp);
		String accessToken = r.get("access_token");
		String refreshToken = r.get("refresh_token");
		long expiresIn = Long.parseLong(r.get("expires_in"));
		// get openid:
		String resp2 = HttpUtil.httpGet(
				"https://graph.qq.com/oauth2.0/me?access_token=" + HttpUtil.urlEncode(accessToken), null, null);
		log.info("Response: " + resp2);
		if (resp2.startsWith("callback(")) {
			resp2 = resp2.substring(9, resp2.lastIndexOf(')'));
		}
		Map<String, String> p = JsonUtil.parseAsMap(resp2);
		String authId = p.get("openid");
		// get user info:
		Map<String, String> qs = new HashMap<String, String>();
		qs.put("access_token", accessToken);
		qs.put("oauth_comsumer_key", this.appKey);
		qs.put("appid", this.appKey);
		qs.put("format", "json");
		qs.put("openid", authId);
		String resp3 = HttpUtil.httpGet("https://graph.qq.com/user/get_user_info", qs, null);
		log.info("Response: " + resp3);
		Map<String, String> info = JsonUtil.parseAsMap(resp3);
		// generate auth:
		OAuthAuthentication oa = new OAuthAuthentication();
		oa.accessToken = accessToken;
		oa.refreshToken = refreshToken;
		oa.expiresIn = expiresIn;
		oa.oauthId = authId;
		oa.name = info.get("nickname");
		oa.url = "";
		oa.imageUrl = info.getOrDefault("figureurl_qq_2", info.getOrDefault("figureurl_qq_1", ""));
		return oa;
	}

}
