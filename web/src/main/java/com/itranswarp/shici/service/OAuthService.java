package com.itranswarp.shici.service;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.itranswarp.shici.auth.CookieAuth;
import com.itranswarp.shici.auth.CookieAuthenticatorHelper;
import com.itranswarp.shici.model.OAuth;
import com.itranswarp.shici.model.User;
import com.itranswarp.shici.oauth.OAuthAuthentication;
import com.itranswarp.shici.oauth.OAuthProvider;
import com.itranswarp.shici.util.HttpUtil;
import com.itranswarp.shici.util.JsonUtil;
import com.itranswarp.warpdb.IdUtil;
import com.itranswarp.warpdb.context.UserContext;

@Controller
public class OAuthService extends AbstractService {

	@Autowired
	CookieAuthenticatorHelper cookieAuthHelper;

	@Autowired
	OAuthProvider[] providers = new OAuthProvider[0];

	@Autowired
	UserService userService;

	Map<String, OAuthProvider> providerMap;

	@PostConstruct
	public void init() {
		this.providerMap = new HashMap<String, OAuthProvider>();
		for (OAuthProvider provider : this.providers) {
			providerMap.put(provider.getName(), provider);
		}
	}

	@RequestMapping(value = "/auth/from/{provider}", method = RequestMethod.GET)
	public void authFrom(@PathVariable("provider") String providerName, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		OAuthProvider provider = providerMap.get(providerName);
		if (provider == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		String redirectUri = getServerUrl(request) + "/auth/callback/" + providerName;
		String jscallback = request.getParameter("jscallback");
		if (jscallback != null) {
			redirectUri = redirectUri + "?jscallback=" + jscallback;
		} else {
			String redirect = getReferer(request);
			redirectUri = redirectUri + "?redirect=" + HttpUtil.urlEncode(redirect);
		}
		String redirect = provider.getAuthenticateURL(redirectUri);
		response.sendRedirect(redirect);
	}

	@RequestMapping(value = "/auth/callback/{provider}", method = RequestMethod.GET)
	public void authCallback(final @PathVariable("provider") String providerName,
			@RequestParam(value = "error", defaultValue = "") String error, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		OAuthProvider provider = providerMap.get(providerName);
		if (provider == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		if (!error.isEmpty()) {
			response.setContentType("text/html;charset=utf-8");
			PrintWriter pw = response.getWriter();
			pw.write("<html><body><p>登录失败，请关闭窗口重试。</p></body></html>");
			pw.flush();
			return;
		}
		String code = request.getParameter("code");
		String redirectUri = getServerUrl(request) + "/auth/callback/" + providerName;
		OAuthAuthentication oa = provider.getAuthentication(redirectUri, code);
		// try find oauth user:
		final String oauthId = providerName + ":" + oa.oauthId;
		OAuth oauth = database.from(OAuth.class).where("oauthId=?", oauthId).first();
		User user = null;
		if (oauth == null) {
			// first time to sign in:
			log.info("First time to sign in: " + oa.name);
			user = new User();
			user.id = IdUtil.next();
			user.email = user.id + "#" + providerName;
			user.name = oa.name;
			user.gender = "";
			user.imageUrl = oa.imageUrl;
			oauth = new OAuth();
			oauth.oauthId = oauthId;
			oauth.userId = user.id;
			oauth.provider = providerName;
			oauth.accessToken = oa.accessToken;
			oauth.refreshToken = oa.refreshToken;
			oauth.expiresAt = oa.getExpiresAt();
			try (UserContext<User> ctx = new UserContext<User>(user)) {
				database.save(oauth, user);
			}
		} else {
			// not first time, update token:
			oauth.accessToken = oa.accessToken;
			oauth.refreshToken = oa.refreshToken;
			oauth.expiresAt = oa.getExpiresAt();
			// try find user:
			user = database.get(User.class, oauth.userId);
			try (UserContext<User> ctx = new UserContext<User>(user)) {
				database.updateProperties(oauth, "accessToken", "refreshToken", "expiresAt");
			}
		}
		// generate cookie:
		generateCookie(response, user, oauth);
		// js callback:
		String jscallback = request.getParameter("jscallback");
		if (jscallback != null) {
			response.setContentType("text/html;charset=utf-8");
			PrintWriter pw = response.getWriter();
			pw.write("<html><body><script> window.opener.");
			pw.write(jscallback);
			pw.write("(");
			pw.write(JsonUtil.toJson(user));
			pw.write("); self.close(); </script></body></html>");
			pw.flush();
		} else {
			String redirect = request.getParameter("redirect");
			response.sendRedirect(redirect == null ? "/" : redirect);
		}
	}

	@RequestMapping(value = "/auth/signout", method = RequestMethod.GET)
	public void signout(HttpServletRequest request, HttpServletResponse response) throws Exception {
		cookieAuthHelper.deleteSessionCookie(response);
		response.sendRedirect(getReferer(request));
	}

	String getServerUrl(HttpServletRequest request) {
		String scheme = HttpUtil.getScheme(request);
		return scheme + "://" + request.getServerName();
	}

	void generateCookie(HttpServletResponse response, User user, OAuth oauth) {
		String cookieValue = cookieAuthHelper
				.encode(new CookieAuth(user.id, oauth.provider, oauth.id, oauth.accessToken), user.salt);
		cookieAuthHelper.setSessionCookie(response, cookieValue);
	}

	String getReferer(HttpServletRequest request) {
		String url = request.getHeader("Referer");
		if (url == null || url.contains("/auth/") || url.contains("/my/") || url.contains("/manage/")) {
			url = "/";
		}
		return url;
	}
}
