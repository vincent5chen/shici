package com.itranswarp.shici.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.itranswarp.shici.context.UserContext;
import com.itranswarp.shici.exception.APIAuthenticationException;
import com.itranswarp.shici.exception.APIPermissionException;
import com.itranswarp.shici.model.User;
import com.itranswarp.warpdb.WarpDb;

public abstract class AbstractService {

	protected final Log log = LogFactory.getLog(getClass());

	@Autowired
	protected WarpDb warpdb;

	public void assertAdminRole() {
		assertRole(User.Role.ADMIN);
	}

	public void assertEditorRole() {
		assertRole(User.Role.EDITOR);
	}

	public void assertUserRole() {
		assertRole(User.Role.USER);
	}

	private void assertRole(long role) {
		User user = UserContext.getCurrentUser();
		if (user == null) {
			throw new APIAuthenticationException("您尚未登录，请先登录。");
		}
		if (user.role > role) {
			throw new APIPermissionException();
		}
	}
}
