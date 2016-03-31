package com.itranswarp.shici.service;

import org.junit.Before;

import com.itranswarp.shici.DatabaseTestBase;
import com.itranswarp.shici.TestHelper;
import com.itranswarp.shici.bean.PoemBean;
import com.itranswarp.shici.bean.PoetBean;
import com.itranswarp.shici.model.Hanz;
import com.itranswarp.shici.model.User;
import com.itranswarp.warpdb.IdUtil;
import com.itranswarp.warpdb.context.UserContext;

public class AbstractServiceTestBase extends DatabaseTestBase {

	protected HanzService hanzService;

	protected User adminUser;
	protected User editorUser;
	protected User normalUser;

	@Before
	public void initDb() {
		// init hanz:
		try (UserContext<User> context = new UserContext<User>(User.SYSTEM)) {
			super.database.save(newHanz('汉', '漢'), newHanz('国', '國'), newHanz('陈', '陳'), newHanz('见', '見'),
					newHanz('张', '張'), newHanz('来', '來'), newHanz('还', '還'), newHanz('详', '詳'));
		}
		hanzService = new HanzService();
		hanzService.database = super.database;
		hanzService.init();
		// init users by each role:
		adminUser = newUser(User.Role.ADMIN, "Admin");
		editorUser = newUser(User.Role.EDITOR, "Editor");
		normalUser = newUser(User.Role.USER, "User");
		User[] users = new User[] { adminUser, editorUser, normalUser };
		for (User user : users) {
			try (UserContext<User> context = new UserContext<User>(User.SYSTEM)) {
				super.database.save(user);
			}
		}
	}

	// helper /////////////////////////////////////////////////////////////////

	Hanz newHanz(char s, char t) {
		Hanz hz = new Hanz();
		hz.s = new String(new char[] { s });
		hz.t = new String(new char[] { t });
		return hz;
	}

	protected String getResourceUrlPrefix() {
		return "//" + TestHelper.getProperty("storage.resource.host");
	}

	protected PoetBean newPoetBean(String dynastyId, String name) {
		PoetBean bean = new PoetBean();
		bean.dynastyId = dynastyId;
		bean.name = name;
		bean.description = "简介：" + name;
		return bean;
	}

	protected PoemBean newPoemBean(String poetId, String name, String content) {
		PoemBean bean = new PoemBean();
		bean.poetId = poetId;
		bean.name = name;
		bean.content = content;
		bean.appreciation = "赏析：" + name;
		return bean;
	}

	protected User newUser(long role, String name) {
		User user = new User();
		user.id = IdUtil.next();
		user.role = role;
		user.name = name;
		user.email = name.toLowerCase() + "@test.com";
		user.gender = User.Gender.MALE;
		user.imageUrl = "http://test/blank.png";
		user.lockedUntil = 0;
		return user;
	}

}
