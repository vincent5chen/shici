package com.itranswarp.shici.service;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.itranswarp.shici.cache.Cache;
import com.itranswarp.shici.exception.APIEntityNotFoundException;
import com.itranswarp.shici.exception.APIPermissionException;
import com.itranswarp.shici.model.User;
import com.itranswarp.shici.util.MapUtil;
import com.itranswarp.warpdb.PagedResults;

@RestController
public class UserService extends AbstractService {

	final Log log = LogFactory.getLog(getClass());

	@Autowired
	Cache cache;

	/**
	 * Fetch user by id, or null if not found.
	 * 
	 * @param id
	 * @return
	 */
	public User fetchUser(String id) {
		return database.fetch(User.class, id);
	}

	/**
	 * Get user by id, or throw APIEntityNotFoundException if not found.
	 * 
	 * @param id
	 * @return
	 */
	public User getUser(String id) {
		User user = database.fetch(User.class, id);
		if (user == null) {
			throw new APIEntityNotFoundException(User.class);
		}
		return user;
	}

	public List<User> getEditors() {
		return database.from(User.class).where("role <= ?", User.Role.EDITOR).orderBy("name").list();
	}

	@RequestMapping(value = "/api/users", method = RequestMethod.GET)
	public PagedResults<User> restGetUsers(@RequestParam(value = "page", defaultValue = "1") int pageIndex) {
		assertEditorRole();
		return database.from(User.class).orderBy("createdAt desc").list(pageIndex);
	}

	@RequestMapping(value = "/api/editors", method = RequestMethod.GET)
	public Map<String, List<User>> restGetEditors() {
		assertEditorRole();
		return MapUtil.createMap("results", getEditors());
	}

	@RequestMapping(value = "/api/users/{id}/lock/{seconds}", method = RequestMethod.POST)
	public User restLockUser(@PathVariable(value = "id") String id, @PathVariable(value = "seconds") int seconds) {
		assertEditorRole();
		User user = getUser(id);
		if (user.role == User.Role.ADMIN) {
			throw new APIPermissionException("Cannot lock admin user.");
		}
		if (seconds <= 0) {
			user.lockedUntil = 0;
		} else {
			user.lockedUntil = System.currentTimeMillis() + seconds * 1000L;
		}
		database.updateProperties(user, "lockedUntil");
		return user;
	}

}
