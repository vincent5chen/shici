package com.itranswarp.shici.oauth;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.itranswarp.shici.util.HttpUtil;
import com.itranswarp.shici.util.JsonUtil;

@Component
public class WeiboOAuthProvider implements OAuthProvider {

	final Log log = LogFactory.getLog(getClass());

	@Value("${oauth.weibo.appkey}")
	String appKey;

	@Value("${oauth.weibo.appsecret}")
	String appSecret;

	@Override
	public String getName() {
		return "weibo";
	}

	@Override
	public String getAuthenticateURL(String redirectUri) {
		StringBuilder sb = new StringBuilder(128);
		sb.append("https://api.weibo.com/oauth2/authorize?client_id=").append(appKey)
				.append("&response_type=code&redirect_uri=").append(HttpUtil.urlEncode(redirectUri));
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
		String resp = HttpUtil.httpPost("https://api.weibo.com/oauth2/access_token", HttpUtil.urlEncode(params), null);
		log.info("Response body: " + resp);
		Map<String, Object> r = JsonUtil.parseAsMap(resp);
		String accessToken = (String) r.get("access_token");
		String authId = (String) r.get("uid");
		long expiresIn = ((Number) r.get("expires_in")).longValue();
		// get profile:
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Authorization", "OAuth2 " + accessToken);
		String resp2 = HttpUtil.httpGet("https://api.weibo.com/2/users/show.json?uid=" + authId, null, headers);
		log.info("Response body: " + resp2);
		Map<String, String> profile = JsonUtil.parseAsMap(resp2);
		// generate auth:
		OAuthAuthentication oa = new OAuthAuthentication();
		oa.accessToken = accessToken;
		oa.refreshToken = "";
		oa.expiresIn = expiresIn;
		oa.name = profile.get("screen_name");
		oa.url = "http://weibo.com/" + profile.getOrDefault("domain", profile.get("idstr"));
		oa.imageUrl = profile.getOrDefault("profile_image_url", "about:blank");
		return oa;
	}

}
