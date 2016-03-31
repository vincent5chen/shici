package com.itranswarp.shici.service;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.itranswarp.shici.bean.PoetBean;
import com.itranswarp.shici.exception.APIArgumentException;
import com.itranswarp.shici.exception.APIPermissionException;
import com.itranswarp.shici.model.Dynasty;
import com.itranswarp.shici.model.Poet;
import com.itranswarp.shici.model.User;
import com.itranswarp.warpdb.Database;
import com.itranswarp.warpdb.EntityConflictException;
import com.itranswarp.warpdb.EntityNotFoundException;
import com.itranswarp.warpdb.IdUtil;
import com.itranswarp.warpdb.context.UserContext;

public class PoemServiceTest extends AbstractServiceTestBase {

	PoemService poemService;

	@Before
	public void setUp() {
		poemService = initPoemService(database);
		poemService.hanzService = hanzService;
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
			dyn.nameCht = hanzService.toCht(dyn.name);
			dyn.descriptionCht = hanzService.toCht(dyn.description);
			dyn.displayOrder = i;
			database.save(dyn);
		}
	}

	Dynasty getTangDynasty() {
		return poemService.getDynasties().get(4);
	}

	// dynasty ////////////////////////////////////////////////////////////////

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

	// poet ///////////////////////////////////////////////////////////////////

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
		PoetBean bean = newPoetBean(getTangDynasty().id, "  \u3000\r\n ");
		try (UserContext<User> context = new UserContext<User>(super.editorUser)) {
			poemService.createPoet(bean);
		}
	}

	@Test(expected = APIArgumentException.class)
	public void testCreatePoetFailedForInvalidDynastyId() {
		PoetBean bean = newPoetBean("123", "陈子昂");
		try (UserContext<User> context = new UserContext<User>(super.editorUser)) {
			poemService.createPoet(bean);
		}
	}

	@Test(expected = EntityNotFoundException.class)
	public void testCreatePoetFailedForNonExistDynastyId() {
		PoetBean bean = newPoetBean(IdUtil.next(), "陈子昂");
		try (UserContext<User> context = new UserContext<User>(super.editorUser)) {
			poemService.createPoet(bean);
		}
	}

	@Test
	public void testCreatePoetOK() {
		PoetBean bean = newPoetBean(getTangDynasty().id, "陈子昂 \u3000\r\n");
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
			assertEquals(0, poet.poemCount);
		}
	}

	@Test(expected = EntityNotFoundException.class)
	public void testGetPoetFailedForNonExist() {
		poemService.getPoet(IdUtil.next());
	}

	@Test
	public void testGetPoetOK() {
		Poet created = null;
		try (UserContext<User> context = new UserContext<User>(super.editorUser)) {
			created = poemService.createPoet(newPoetBean(getTangDynasty().id, "陈子昂"));
		}
		Poet query = poemService.getPoet(created.id);
		assertEquals("陈子昂", query.name);
		assertEquals("陳子昂", query.nameCht);
		assertEquals("简介：陈子昂", query.description);
		assertEquals("简介：陳子昂", query.descriptionCht);
	}

	@Test
	public void testGetPoetsButEmpty() {
		List<Poet> poets = poemService.getPoets(getTangDynasty().id);
		assertNotNull(poets);
		assertTrue(poets.isEmpty());
	}

	@Test
	public void testGetPoetsWith3() {
		try (UserContext<User> context = new UserContext<User>(super.editorUser)) {
			poemService.createPoet(newPoetBean(getTangDynasty().id, "C-陈子昂"));
			poemService.createPoet(newPoetBean(getTangDynasty().id, "L-李白"));
			poemService.createPoet(newPoetBean(getTangDynasty().id, "D-杜甫"));
		}
		List<Poet> poets = poemService.getPoets(getTangDynasty().id);
		assertNotNull(poets);
		assertEquals(3, poets.size());
		assertEquals("C-陈子昂", poets.get(0).name);
		assertEquals("D-杜甫", poets.get(1).name);
		assertEquals("L-李白", poets.get(2).name);
	}

	@Test(expected = APIPermissionException.class)
	public void testUpdatePoetFailedWithoutPermission() {
		try (UserContext<User> context = new UserContext<User>(super.normalUser)) {
			poemService.updatePoet(null, null);
		}
	}

	@Test(expected = APIArgumentException.class)
	public void testUpdatePoetFailedWithBadName() {
		try (UserContext<User> context = new UserContext<User>(super.editorUser)) {
			Poet poet = poemService.createPoet(newPoetBean(getTangDynasty().id, "陈子昂"));
			poemService.updatePoet(poet.id, newPoetBean(getTangDynasty().id, " [ ]\n "));
		}
	}

	@Test(expected = EntityNotFoundException.class)
	public void testUpdatePoetFailedWithBadDynastyId() {
		try (UserContext<User> context = new UserContext<User>(super.editorUser)) {
			Poet poet = poemService.createPoet(newPoetBean(getTangDynasty().id, "陈子昂"));
			poemService.updatePoet(poet.id, newPoetBean(IdUtil.next(), "子昂"));
		}
	}

	@Test(expected = EntityNotFoundException.class)
	public void testUpdatePoetFailedWithBadPoetId() {
		try (UserContext<User> context = new UserContext<User>(super.editorUser)) {
			poemService.createPoet(newPoetBean(getTangDynasty().id, "陈子昂"));
			poemService.updatePoet(IdUtil.next(), newPoetBean(getTangDynasty().id, "子昂"));
		}
	}

	@Test
	public void testUpdatePoetOK() {
		try (UserContext<User> context = new UserContext<User>(super.editorUser)) {
			Poet poet = poemService.createPoet(newPoetBean(getTangDynasty().id, "陈子昂"));
			poemService.updatePoet(poet.id, newPoetBean(getTangDynasty().id, "张子昂"));
			// check:
			Poet p = poemService.getPoet(poet.id);
			assertEquals("张子昂", p.name);
			assertEquals("張子昂", p.nameCht);
			assertEquals("简介：张子昂", p.description);
			assertEquals("简介：張子昂", p.descriptionCht);
		}
	}

	@Test(expected = APIPermissionException.class)
	public void testDeletePoetFailedWithoutPermission() {
		try (UserContext<User> context = new UserContext<User>(super.normalUser)) {
			poemService.deletePoet(null);
		}
	}

	@Test(expected = EntityConflictException.class)
	public void testDeletePoetFailedForPoemExist() {
		try (UserContext<User> context = new UserContext<User>(super.editorUser)) {
			Poet poet = poemService.createPoet(newPoetBean(getTangDynasty().id, "陈子昂"));
			poemService.createPoem(newPoemBean(poet.id, "登幽州台歌", "前不见古人，后不见来者"));
			poemService.deletePoet(poet.id);
		}
	}

	// poem ///////////////////////////////////////////////////////////////////

}
