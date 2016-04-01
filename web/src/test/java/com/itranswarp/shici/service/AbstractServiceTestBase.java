package com.itranswarp.shici.service;

import org.junit.Before;

import com.itranswarp.shici.DatabaseTestBase;
import com.itranswarp.shici.TestHelper;
import com.itranswarp.shici.bean.PoemBean;
import com.itranswarp.shici.bean.PoetBean;
import com.itranswarp.shici.model.Dynasty;
import com.itranswarp.shici.model.Hanz;
import com.itranswarp.shici.model.Poem;
import com.itranswarp.shici.model.User;
import com.itranswarp.warpdb.Database;
import com.itranswarp.warpdb.IdUtil;
import com.itranswarp.warpdb.context.UserContext;

public class AbstractServiceTestBase extends DatabaseTestBase {

	protected User adminUser;
	protected User editorUser;
	protected User normalUser;

	@Before
	public void initDb() {
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

	protected void initDynasties(HanzService hanzService, Database db) {
		try (UserContext<User> context = new UserContext<User>(User.SYSTEM)) {
			String[] names = { "先秦", "汉代", "三国两晋", "南北朝", "隋唐", "宋代", "元代", "明代", "清代", "近现代", "不详" };
			for (int i = 0; i < names.length; i++) {
				Dynasty dyn = new Dynasty();
				dyn.name = names[i];
				dyn.description = "朝代：" + names[i];
				dyn.nameCht = hanzService.toCht(dyn.name);
				dyn.descriptionCht = hanzService.toCht(dyn.description);
				dyn.displayOrder = i;
				database.save(dyn);
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
		bean.birth = "";
		bean.death = "";
		return bean;
	}

	protected PoemBean newPoemBean(String poetId, String name, String content) {
		PoemBean bean = new PoemBean();
		bean.poetId = poetId;
		bean.name = name;
		bean.content = content;
		bean.appreciation = "赏析：" + name;
		bean.form = Poem.Form.WU_LV;
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
