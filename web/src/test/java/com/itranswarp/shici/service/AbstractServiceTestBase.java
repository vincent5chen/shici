package com.itranswarp.shici.service;

import java.time.LocalDate;
import java.util.Arrays;

import org.junit.Before;

import com.itranswarp.shici.DatabaseTestBase;
import com.itranswarp.shici.TestHelper;
import com.itranswarp.shici.bean.CategoryBean;
import com.itranswarp.shici.bean.CategoryPoemBean;
import com.itranswarp.shici.bean.FeaturedBean;
import com.itranswarp.shici.bean.PoemBean;
import com.itranswarp.shici.bean.PoetBean;
import com.itranswarp.shici.context.UserContext;
import com.itranswarp.shici.model.Dynasty;
import com.itranswarp.shici.model.Hanzi;
import com.itranswarp.shici.model.Poem;
import com.itranswarp.shici.model.User;
import com.itranswarp.shici.util.IdUtils;
import com.itranswarp.warpdb.WarpDb;

public abstract class AbstractServiceTestBase extends DatabaseTestBase {

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
			try (UserContext context = new UserContext(User.SYSTEM)) {
				super.warpdb.save(user);
			}
		}
	}

	protected void initDynasties(HanziService hanzService, WarpDb warpdb) {
		try (UserContext context = new UserContext(User.SYSTEM)) {
			String[] names = { "先秦", "汉代", "三国两晋", "南北朝", "隋唐", "宋代", "元代", "明代", "清代", "近现代", "不详" };
			for (int i = 0; i < names.length; i++) {
				Dynasty dyn = new Dynasty();
				dyn.name = names[i];
				dyn.nameCht = hanzService.toCht(dyn.name);
				dyn.displayOrder = i;
				warpdb.save(dyn);
			}
		}
	}

	// helper /////////////////////////////////////////////////////////////////

	Hanzi newHanz(char s, char t) {
		Hanzi hz = new Hanzi();
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

	protected FeaturedBean newFeaturedBean(String poemId, LocalDate pubDate) {
		FeaturedBean bean = new FeaturedBean();
		bean.poemId = poemId;
		bean.pubDate = pubDate;
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

	protected CategoryBean newCategoryBean(String name) {
		CategoryBean bean = new CategoryBean();
		bean.name = name;
		bean.description = "简介：" + name;
		return bean;
	}

	protected CategoryPoemBean newCategoryPoemBean(String sectionName, String... poemIds) {
		CategoryPoemBean bean = new CategoryPoemBean();
		bean.sectionName = sectionName;
		bean.ids = Arrays.asList(poemIds);
		return bean;
	}

	protected User newUser(long role, String name) {
		User user = new User();
		user.id = IdUtils.next();
		user.role = role;
		user.name = name;
		user.email = name.toLowerCase() + "@test.com";
		user.gender = User.Gender.MALE;
		user.imageUrl = "http://test/blank.png";
		user.lockedUntil = 0;
		return user;
	}

}
