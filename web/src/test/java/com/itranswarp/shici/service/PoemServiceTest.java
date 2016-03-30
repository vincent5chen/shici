package com.itranswarp.shici.service;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.itranswarp.shici.bean.PoetBean;
import com.itranswarp.shici.exception.APIArgumentException;
import com.itranswarp.shici.exception.APIPermissionException;
import com.itranswarp.shici.model.Dynasty;
import com.itranswarp.shici.model.Poet;
import com.itranswarp.shici.model.User;
import com.itranswarp.warpdb.Database;
import com.itranswarp.warpdb.EntityNotFoundException;
import com.itranswarp.warpdb.IdUtil;
import com.itranswarp.warpdb.context.UserContext;

public class PoemServiceTest extends AbstractServiceTestBase {

	PoemService poemService;

	@Before
	public void setUp() {
		poemService = initPoemService(database);
	}

	public PoemService initPoemService(Database db) {
		PoemService s = new PoemService();
		s.database = db;
		try (UserContext<User> context = new UserContext<User>(User.SYSTEM)) {
			initDynasties(db);
		}
		return s;
	}

	void initDynasties(Database db) {
		String[] names = { "先秦", "汉代", "三国两晋", "南北朝", "隋唐", "宋代", "元代", "明代", "清代", "近现代", "不详" };
		for (int i = 0; i < names.length; i++) {
			Dynasty dyn = new Dynasty();
			dyn.name = names[i];
			dyn.description = "朝代：" + names[i];
			dyn.nameCht = HanzService.toCht(dyn.name);
			dyn.descriptionCht = HanzService.toCht(dyn.description);
			dyn.displayOrder = i;
			database.save(dyn);
		}
	}

	Dynasty getTangDynasty() {
		return poemService.getDynasties().get(4);
	}

	@Test
	public void testGetDynasties() {
		List<Dynasty> dynasties = poemService.getDynasties();
		assertNotNull(dynasties);
		assertEquals(11, dynasties.size());
		Dynasty han = dynasties.get(1);
		assertEquals("汉代", han.name);
		assertEquals("漢代", han.nameCht);
		assertEquals("朝代：汉代", han.description);
		assertEquals("朝代：漢代", han.descriptionCht);
	}

	@Test(expected = EntityNotFoundException.class)
	public void testGetDynastyNotFound() {
		poemService.getDynasty(IdUtil.next());
	}

	@Test
	public void testGetDynastyOK() {
		Dynasty dyn = poemService.getDynasty(getTangDynasty().id);
		assertEquals("隋唐", dyn.name);
	}

	@Test
	public void testGetPoetButEmpty() {
		List<Poet> poets = poemService.getPoets(getTangDynasty().id);
		assertNotNull(poets);
		assertTrue(poets.isEmpty());
	}

	@Test(expected = APIPermissionException.class)
	public void testCreatePoetFailedWithoutPermission() {
		try (UserContext<User> context = new UserContext<User>(super.normalUser)) {
			poemService.createPoet(null);
		}
	}

	@Test(expected = APIArgumentException.class)
	public void testCreatePoetFailedForInvalidName() {
		PoetBean bean = newPoetBean(getTangDynasty(), "   ");
		try (UserContext<User> context = new UserContext<User>(super.editorUser)) {
			poemService.createPoet(bean);
		}
	}

	@Test
	public void testCreatePoetOK() {
		PoetBean bean = newPoetBean(getTangDynasty(), "陈子昂 \u3000\r\n");
		bean.birth = "  659  \t";
		bean.death = "\n 700 \r";
		try (UserContext<User> context = new UserContext<User>(super.editorUser)) {
			Poet poet = poemService.createPoet(bean);
			assertNotNull(poet);
			assertEquals("陈子昂", poet.name);
			assertEquals("陳子昂", poet.nameCht);
			assertEquals("简介：陈子昂", poet.description);
			assertEquals("简介：陳子昂", poet.descriptionCht);
			assertEquals("659", poet.birth);
			assertEquals("700", poet.death);
		}
	}
}
