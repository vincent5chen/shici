package com.itranswarp.shici.oauth;

public class OAuthAuthentication {

	public String accessToken;
	public String refreshToken;
	public long expiresIn;
	public String oauthId;
	public String name;
	public String url;
	public String imageUrl;

	public long getExpiresAt() {
		// max to 15 days:
		long exp = expiresIn > 1296000L ? 1296000L : expiresIn;
		return System.currentTimeMillis() + exp * 1000L;
	}
}
