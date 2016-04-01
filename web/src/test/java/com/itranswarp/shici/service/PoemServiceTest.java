package com.itranswarp.shici.service;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.dao.DataIntegrityViolationException;

import com.itranswarp.shici.bean.PoemBean;
import com.itranswarp.shici.bean.PoetBean;
import com.itranswarp.shici.exception.APIArgumentException;
import com.itranswarp.shici.exception.APIPermissionException;
import com.itranswarp.shici.model.Dynasty;
import com.itranswarp.shici.model.Poem;
import com.itranswarp.shici.model.Poet;
import com.itranswarp.shici.model.Resource;
import com.itranswarp.shici.model.User;
import com.itranswarp.shici.util.Base64Util;
import com.itranswarp.shici.util.FileUtil;
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

	Dynasty getSongDynasty() {
		return poemService.getDynasties().get(5);
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

	@Test(expected = DataIntegrityViolationException.class)
	public void testDeletePoetFailedForPoemExist() {
		try (UserContext<User> context = new UserContext<User>(super.editorUser)) {
			Poet poet = poemService.createPoet(newPoetBean(getTangDynasty().id, "陈子昂"));
			poemService.createPoem(newPoemBean(poet.id, "登幽州台歌", "前不见古人，后不见来者"));
			poemService.deletePoet(poet.id);
		}
	}

	@Test
	public void testDeletePoetOK() {
		try (UserContext<User> context = new UserContext<User>(super.editorUser)) {
			Poet poet = poemService.createPoet(newPoetBean(getTangDynasty().id, "陈子昂"));
			poemService.deletePoet(poet.id);
		}
	}

	// poem ///////////////////////////////////////////////////////////////////

	@Test(expected = EntityNotFoundException.class)
	public void testGetPoemButNotFound() {
		poemService.getPoem(IdUtil.next());
	}

	@Test(expected = APIPermissionException.class)
	public void testCreatePoemFailedWithoutPermission() {
		try (UserContext<User> context = new UserContext<User>(super.editorUser)) {
			poemService.createPoet(newPoetBean(getTangDynasty().id, "陈子昂"));
		}
		try (UserContext<User> context = new UserContext<User>(super.normalUser)) {
			poemService.createPoem(null);
		}
	}

	@Test(expected = APIArgumentException.class)
	public void testCreatePoemFailedWithBadName() {
		try (UserContext<User> context = new UserContext<User>(super.editorUser)) {
			Poet poet = poemService.createPoet(newPoetBean(getTangDynasty().id, "陈子昂"));
			poemService.createPoem(newPoemBean(poet.id, "   \u3000  ", "前不见古人，后不见来者"));
		}
	}

	@Test(expected = APIArgumentException.class)
	public void testCreatePoemFailedWithBadContent() {
		try (UserContext<User> context = new UserContext<User>(super.editorUser)) {
			Poet poet = poemService.createPoet(newPoetBean(getTangDynasty().id, "陈子昂"));
			poemService.createPoem(newPoemBean(poet.id, "登幽州台歌", "  \n \t  \u3000  "));
		}
	}

	@Test(expected = APIArgumentException.class)
	public void testCreatePoemFailedWithBadForm() {
		try (UserContext<User> context = new UserContext<User>(super.editorUser)) {
			Poet poet = poemService.createPoet(newPoetBean(getTangDynasty().id, "陈子昂"));
			PoemBean bean = newPoemBean(poet.id, "登幽州台歌", "前不见古人，后不见来者");
			bean.form = 1234;
			poemService.createPoem(bean);
		}
	}

	@Test(expected = EntityNotFoundException.class)
	public void testCreatePoemFailedWithBadPoetId() {
		try (UserContext<User> context = new UserContext<User>(super.editorUser)) {
			poemService.createPoem(newPoemBean(IdUtil.next(), "登幽州台歌", "前不见古人，后不见来者"));
		}
	}

	@Test
	public void testCreatePoemWithoutImageAndGetOK() {
		Poet poet = null;
		Poem poem = null;
		try (UserContext<User> context = new UserContext<User>(super.editorUser)) {
			poet = poemService.createPoet(newPoetBean(getTangDynasty().id, "陈子昂"));
			PoemBean bean = newPoemBean(poet.id, " 登●幽 州 台[1]歌", "前不(2)见古人，\n后不见③来者 \t\n  \u3000");
			bean.tags = "  唐诗，\t古诗；唐代 古代,诗词";
			poem = poemService.createPoem(bean);
		}
		Poem p = poemService.getPoem(poem.id);
		assertNotNull(p);
		assertEquals(poet.id, p.poetId);
		assertEquals(poet.name, p.poetName);
		assertEquals(poet.nameCht, p.poetNameCht);
		assertEquals(poet.dynastyId, p.dynastyId);
		assertEquals("唐诗,古诗,唐代,古代,诗词", p.tags);
		assertEquals("登·幽州台歌", p.name);
		assertEquals("登·幽州臺歌", p.nameCht);
		assertEquals("前不见古人，后不见来者", p.content);
		assertEquals("前不見古人，後不見來者", p.contentCht);
		assertEquals("赏析：登·幽州台歌", p.appreciation);
		assertEquals("赏析：登·幽州臺歌", p.appreciationCht);
		assertEquals(Poem.Form.WU_LV, p.form);
		assertEquals("", p.imageId);
		assertEquals(1, poet.poemCount);
	}

	@Test
	public void testCreatePoemWithImageAndGetOK() throws IOException {
		Poet poet = null;
		Poem poem = null;
		try (UserContext<User> context = new UserContext<User>(super.editorUser)) {
			poet = poemService.createPoet(newPoetBean(getTangDynasty().id, "陈子昂"));
			PoemBean bean = newPoemBean(poet.id, "登幽州台歌", "前不见古人，后不见来者");
			bean.imageData = Base64Util.encodeToString(FileUtil.getResource("/640x360.jpg"));
			poem = poemService.createPoem(bean);
		}
		Poem p = poemService.getPoem(poem.id);
		// check image:
		assertFalse(p.imageId.isEmpty());
		Resource resource = poemService.getResource(p.imageId);
		assertEquals(p.imageId, resource.id);
		assertEquals(Base64Util.encodeToString(FileUtil.getResource("/640x360.jpg")), resource.data);
	}

	@Test
	public void testCreatePoemWithLargeImageAndGetOK() throws IOException {
		Poet poet = null;
		Poem poem = null;
		try (UserContext<User> context = new UserContext<User>(super.editorUser)) {
			poet = poemService.createPoet(newPoetBean(getTangDynasty().id, "陈子昂"));
			PoemBean bean = newPoemBean(poet.id, "登幽州台歌", "前不见古人，后不见来者");
			bean.imageData = Base64Util.encodeToString(FileUtil.getResource("/1280x800.jpg"));
			poem = poemService.createPoem(bean);
		}
		Poem p = poemService.getPoem(poem.id);
		// check image:
		assertFalse(p.imageId.isEmpty());
		Resource resource = poemService.getResource(p.imageId);
		assertEquals(p.imageId, resource.id);
		assertNotEquals(Base64Util.encodeToString(FileUtil.getResource("/1280x800.jpg")), resource.data);
	}

	@Test(expected = APIPermissionException.class)
	public void testUpdatePoemFailedWithoutPermission() {
		try (UserContext<User> context = new UserContext<User>(super.normalUser)) {
			poemService.updatePoem(null, null);
		}
	}

	@Test(expected = APIArgumentException.class)
	public void testUpdatePoemFailedWithBadName() {
		Poet poet = null;
		Poem poem = null;
		try (UserContext<User> context = new UserContext<User>(super.editorUser)) {
			poet = poemService.createPoet(newPoetBean(getTangDynasty().id, "陈子昂"));
			poem = poemService.createPoem(newPoemBean(poet.id, "登幽州台歌", "前不见古人，后不见来者"));
			poemService.updatePoem(poem.id, newPoemBean(poet.id, "⒈⒉⒊ \n ", "前不见古人，后不见来者"));
		}
	}

	@Test(expected = APIArgumentException.class)
	public void testUpdatePoemFailedWithBadContent() {
		Poet poet = null;
		Poem poem = null;
		try (UserContext<User> context = new UserContext<User>(super.editorUser)) {
			poet = poemService.createPoet(newPoetBean(getTangDynasty().id, "陈子昂"));
			poem = poemService.createPoem(newPoemBean(poet.id, "登幽州台歌", "前不见古人，后不见来者"));
			poemService.updatePoem(poem.id, newPoemBean(poet.id, "登幽州台歌", " \n \u3000 \t\t\r\n(1)\n "));
		}
	}

	@Test(expected = APIArgumentException.class)
	public void testUpdatePoemFailedWithBadForm() {
		Poet poet = null;
		Poem poem = null;
		try (UserContext<User> context = new UserContext<User>(super.editorUser)) {
			poet = poemService.createPoet(newPoetBean(getTangDynasty().id, "陈子昂"));
			poem = poemService.createPoem(newPoemBean(poet.id, "登幽州台歌", "前不见古人，后不见来者"));
			PoemBean bean = newPoemBean(poet.id, "幽州台歌", "前不见，后不见");
			bean.form = 12345;
			poemService.updatePoem(poem.id, bean);
		}
	}

	@Test(expected = APIArgumentException.class)
	public void testUpdatePoemFailedWithBadImage() throws IOException {
		Poet poet = null;
		Poem poem = null;
		try (UserContext<User> context = new UserContext<User>(super.editorUser)) {
			poet = poemService.createPoet(newPoetBean(getTangDynasty().id, "陈子昂"));
			poem = poemService.createPoem(newPoemBean(poet.id, "登幽州台歌", "前不见古人，后不见来者"));
			PoemBean bean = newPoemBean(poet.id, "幽州台歌", "前不见，后不见");
			bean.imageData = Base64Util.encodeToString(FileUtil.getResource("/license.txt"));
			poemService.updatePoem(poem.id, bean);
		}
	}

	@Test(expected = EntityNotFoundException.class)
	public void testUpdatePoemFailedWithBadPoetId() {
		Poet poet = null;
		Poem poem = null;
		try (UserContext<User> context = new UserContext<User>(super.editorUser)) {
			poet = poemService.createPoet(newPoetBean(getTangDynasty().id, "陈子昂"));
			poem = poemService.createPoem(newPoemBean(poet.id, "登幽州台歌", "前不见古人，后不见来者"));
			poemService.updatePoem(poem.id, newPoemBean(IdUtil.next(), "幽州台歌", "前不见，后不见"));
		}
	}

	@Test
	public void testUpdatePoemOK() {
		Poet poet = null;
		Poem poem = null;
		try (UserContext<User> context = new UserContext<User>(super.editorUser)) {
			poet = poemService.createPoet(newPoetBean(getTangDynasty().id, "陈子昂"));
			poem = poemService.createPoem(newPoemBean(poet.id, "登幽州台歌", "前不见古人，后不见来者"));
			PoemBean bean = newPoemBean(poet.id, " 幽州 {1}台歌", "前不见，\n\n后不见");
			bean.tags = " 古诗, 唐代";
			poemService.updatePoem(poem.id, bean);
		}
		// check:
		Poem p = poemService.getPoem(poem.id);
		assertEquals(poet.id, p.poetId);
		assertEquals(poet.name, p.poetName);
		assertEquals(poet.nameCht, p.poetNameCht);
		assertEquals(poet.dynastyId, p.dynastyId);
		assertEquals("古诗,唐代", p.tags);
		assertEquals("幽州台歌", p.name);
		assertEquals("幽州臺歌", p.nameCht);
		assertEquals("前不见，后不见", p.content);
		assertEquals("前不見，後不見", p.contentCht);
		assertEquals("赏析：幽州台歌", p.appreciation);
		assertEquals("赏析：幽州臺歌", p.appreciationCht);
		assertEquals(Poem.Form.WU_LV, p.form);
		assertEquals("", p.imageId);
		assertEquals(1, poet.poemCount);
	}

	@Test
	public void updatePoemOKWithImageAdded() throws IOException {
		Poet poet = null;
		Poem poem = null;
		try (UserContext<User> context = new UserContext<User>(super.editorUser)) {
			poet = poemService.createPoet(newPoetBean(getTangDynasty().id, "陈子昂"));
			poem = poemService.createPoem(newPoemBean(poet.id, "登幽州台歌", "前不见古人，后不见来者"));
			PoemBean bean = newPoemBean(poet.id, " 幽州 {1}台歌", "前不见，\n\n后不见");
			bean.imageData = Base64Util.encodeToString(FileUtil.getResource("/640x360.jpg"));
			poemService.updatePoem(poem.id, bean);
		}
		// check:
		Poem p = poemService.getPoem(poem.id);
		assertFalse(p.imageId.isEmpty());
		Resource resource = poemService.getResource(p.imageId);
		assertEquals(Base64Util.encodeToString(FileUtil.getResource("/640x360.jpg")), resource.data);
	}

	@Test
	public void testUpdatePoemOKWithImageReplaced() throws IOException {
		Poet poet = null;
		Poem poem = null;
		Resource oldRes = null;
		try (UserContext<User> context = new UserContext<User>(super.editorUser)) {
			poet = poemService.createPoet(newPoetBean(getTangDynasty().id, "陈子昂"));
			PoemBean create = newPoemBean(poet.id, "登幽州台歌", "前不见古人，后不见来者");
			create.imageData = Base64Util.encodeToString(FileUtil.getResource("/1280x800.jpg"));
			poem = poemService.createPoem(create);
			oldRes = poemService.getResource(poem.imageId);
			// update with new image:
			PoemBean bean = newPoemBean(poet.id, " 幽州台歌", "前不见，后不见");
			bean.imageData = Base64Util.encodeToString(FileUtil.getResource("/640x360.jpg"));
			poemService.updatePoem(poem.id, bean);
		}
		// check:
		Poem p = poemService.getPoem(poem.id);
		assertFalse(p.imageId.isEmpty());
		Resource newRes = poemService.getResource(p.imageId);
		assertEquals(Base64Util.encodeToString(FileUtil.getResource("/640x360.jpg")), newRes.data);
		// old resource is removed:
		try {
			poemService.getResource(oldRes.id);
			fail("Old resource is not deleted.");
		} catch (EntityNotFoundException e) {
		}
	}

	@Test
	public void testUpdatePoemOKWithImageKeepTheSame() throws IOException {
		Poet poet = null;
		Poem poem = null;
		Resource oldRes = null;
		try (UserContext<User> context = new UserContext<User>(super.editorUser)) {
			poet = poemService.createPoet(newPoetBean(getTangDynasty().id, "陈子昂"));
			PoemBean create = newPoemBean(poet.id, "登幽州台歌", "前不见古人，后不见来者");
			create.imageData = Base64Util.encodeToString(FileUtil.getResource("/1280x800.jpg"));
			poem = poemService.createPoem(create);
			oldRes = poemService.getResource(poem.imageId);
			// update with no image:
			poemService.updatePoem(poem.id, newPoemBean(poet.id, " 幽州台歌", "前不见，后不见"));
		}
		// check:
		Poem p = poemService.getPoem(poem.id);
		assertEquals(oldRes.id, p.imageId);
		Resource newRes = poemService.getResource(p.imageId);
		assertEquals(oldRes.id, newRes.id);
		assertEquals(oldRes.data, newRes.data);
	}

	@Test
	public void testUpdatePoemOKWithPoetChanged() throws IOException {
		Poet poet1 = null;
		Poet poet2 = null;
		Poem poem = null;
		try (UserContext<User> context = new UserContext<User>(super.editorUser)) {
			poet1 = poemService.createPoet(newPoetBean(getTangDynasty().id, "陈子昂"));
			poet2 = poemService.createPoet(newPoetBean(getSongDynasty().id, "苏东坡"));
			poem = poemService.createPoem(newPoemBean(poet1.id, "登幽州台歌", "前不见古人，后不见来者"));
			// update with new poet:
			PoemBean bean = newPoemBean(poet2.id, "水调歌头", "明月几时有，把酒问青天");
			poemService.updatePoem(poem.id, bean);
		}
		// check:
		Poem p = poemService.getPoem(poem.id);
		assertEquals(poet2.id, p.poetId);
		assertEquals(0, poemService.getPoet(poet1.id).poemCount);
		assertEquals(1, poemService.getPoet(poet2.id).poemCount);
	}

	@Test(expected = APIPermissionException.class)
	public void testDeletePoemFailedWithoutPermission() {
		try (UserContext<User> context = new UserContext<User>(super.normalUser)) {
			poemService.deletePoem(null);
		}
	}

	// resource ///////////////////////////////////////////////////////////////

	@Test
	public void testGetSizeOfBase64String() throws IOException {
		for (int i = 0; i < 100; i++) {
			byte[] data = new byte[i];
			String b64 = Base64Util.encodeToString(data);
			int size = poemService.getSizeOfBase64String(b64);
			assertEquals(i, size);
		}
	}
}
