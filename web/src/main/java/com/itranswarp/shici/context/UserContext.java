package com.itranswarp.shici.context;

import com.itranswarp.shici.model.User;

/**
 * UserContext holds current user in current thread.
 * 
 * Using try (resource) { ... } is a MUST:
 * 
 * <code>
 * // start context A:
 * try (UserContext ctx = new UserContext(new User("User-123"))) {
 * 	   User u = UserContext.getCurrentUser(); // User-123
 * }
 * UserContext.getCurrentUser(); // null
 * </code>
 * 
 * @author michael
 */
public class UserContext implements AutoCloseable {

	static final ThreadLocal<User> current = new ThreadLocal<User>();

	public UserContext(User user) {
		current.set(user);
	}

	public static User getRequiredCurrentUser() {
		User user = current.get();
		if (user == null) {
			throw new MissingContextException();
		}
		return user;
	}

	public static User getCurrentUser() {
		return current.get();
	}

	@Override
	public void close() {
		current.remove();
	}

}
