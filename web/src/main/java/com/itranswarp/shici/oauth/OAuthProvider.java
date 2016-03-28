package com.itranswarp.shici.oauth;

import java.io.IOException;

public interface OAuthProvider {

	String getName();

	String getAuthenticateURL(String redirectUri);

	OAuthAuthentication getAuthentication(String redirectUri, String code) throws IOException;

}
