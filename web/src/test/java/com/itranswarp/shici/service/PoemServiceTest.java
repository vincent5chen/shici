package com.itranswarp.shici.service;

import static org.junit.Assert.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityNotFoundException;

import org.junit.Before;
import org.junit.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;

import com.itranswarp.shici.bean.PoemBean;
import com.itranswarp.shici.bean.PoetBean;
import com.itranswarp.shici.context.UserContext;
import com.itranswarp.shici.exception.APIArgumentException;
import com.itranswarp.shici.exception.APIEntityConflictException;
import com.itranswarp.shici.exception.APIPermissionException;
import com.itranswarp.shici.model.Category;
import com.itranswarp.shici.model.Dynasty;
import com.itranswarp.shici.model.Poem;
import com.itranswarp.shici.model.Poet;
import com.itranswarp.shici.model.Resource;
import com.itranswarp.shici.model.User;
import com.itranswarp.shici.service.PoemService.TheCategoryPoem;
import com.itranswarp.shici.service.PoemService.TheFeaturedPoem;
import com.itranswarp.shici.util.Base64Util;
import com.itranswarp.shici.util.FileUtil;
import com.itranswarp.shici.util.IdUtils;
import com.itranswarp.warpdb.WarpDb;

public class PoemServiceTest extends AbstractServiceTestBase {

	PoemService poemService;

	@Before
	public void setUp() {
		poemService = initPoemService(warpdb);
		initDynasties(warpdb);
	}

	public PoemService initPoemService(WarpDb db) {
		PoemService s = new PoemService();
		s.warpdb = db;
		s.hanziService = new HanziServiceTest().initHanziService(db);
		return s;
	}

	void initDynasties(WarpDb db) {
		HanziService hanzService = new HanziServiceTest().initHanziService(db);
		try (UserContext context = new UserContext(User.SYSTEM)) {
			String[] names = { "先秦", "汉代", "三国两晋", "南北朝", "隋唐", "宋代", "元代", "明代", "清代", "近现代", "不详" };
			for (int i = 0; i < names.length; i++) {
				Dynasty dyn = new Dynasty();
				dyn.name = names[i];
				dyn.nameCht = hanzService.toCht(dyn.name);
				dyn.displayOrder = i;
				db.save(dyn);
			}
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
	}

	@Test(expected = EntityNotFoundException.class)
	public void testGetDynastyNotFound() {
		poemService.getDynasty(IdUtils.next());
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
		try (UserContext context = new UserContext(super.normalUser)) {
			poemService.createPoet(null);
		}
	}

	@Test(expected = APIArgumentException.class)
	public void testCreatePoetFailedForInvalidName() {
		PoetBean bean = newPoetBean(getTangDynasty().id, "  \u3000\r\n ");
		try (UserContext context = new UserContext(super.editorUser)) {
			poemService.createPoet(bean);
		}
	}

	@Test(expected = APIArgumentException.class)
	public void testCreatePoetFailedForInvalidDynastyId() {
		PoetBean bean = newPoetBean("123", "陈子昂");
		try (UserContext context = new UserContext(super.editorUser)) {
			poemService.createPoet(bean);
		}
	}

	@Test(expected = EntityNotFoundException.class)
	public void testCreatePoetFailedForNonExistDynastyId() {
		PoetBean bean = newPoetBean(IdUtils.next(), "陈子昂");
		try (UserContext context = new UserContext(super.editorUser)) {
			poemService.createPoet(bean);
		}
	}

	@Test
	public void testCreatePoetOK() {
		PoetBean bean = newPoetBean(getTangDynasty().id, "陳子昂 \u3000\r\n");
		bean.birth = "  659  \t";
		bean.death = "\n 700 \r";
		try (UserContext context = new UserContext(super.editorUser)) {
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
		poemService.getPoet(IdUtils.next());
	}

	@Test
	public void testGetPoetOK() {
		Poet created = null;
		try (UserContext context = new UserContext(super.editorUser)) {
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
		try (UserContext context = new UserContext(super.editorUser)) {
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
		try (UserContext context = new UserContext(super.normalUser)) {
			poemService.updatePoet(null, null);
		}
	}

	@Test(expected = APIArgumentException.class)
	public void testUpdatePoetFailedWithBadName() {
		try (UserContext context = new UserContext(super.editorUser)) {
			Poet poet = poemService.createPoet(newPoetBean(getTangDynasty().id, "陈子昂"));
			poemService.updatePoet(poet.id, newPoetBean(getTangDynasty().id, " [ ]\n "));
		}
	}

	@Test(expected = EntityNotFoundException.class)
	public void testUpdatePoetFailedWithBadDynastyId() {
		try (UserContext context = new UserContext(super.editorUser)) {
			Poet poet = poemService.createPoet(newPoetBean(getTangDynasty().id, "陈子昂"));
			poemService.updatePoet(poet.id, newPoetBean(IdUtils.next(), "子昂"));
		}
	}

	@Test(expected = EntityNotFoundException.class)
	public void testUpdatePoetFailedWithBadPoetId() {
		try (UserContext context = new UserContext(super.editorUser)) {
			poemService.createPoet(newPoetBean(getTangDynasty().id, "陈子昂"));
			poemService.updatePoet(IdUtils.next(), newPoetBean(getTangDynasty().id, "子昂"));
		}
	}

	@Test
	public void testUpdatePoetOK() {
		try (UserContext context = new UserContext(super.editorUser)) {
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
		try (UserContext context = new UserContext(super.normalUser)) {
			poemService.deletePoet(null);
		}
	}

	@Test(expected = DataIntegrityViolationException.class)
	public void testDeletePoetFailedForPoemExist() {
		try (UserContext context = new UserContext(super.editorUser)) {
			Poet poet = poemService.createPoet(newPoetBean(getTangDynasty().id, "陈子昂"));
			poemService.createPoem(newPoemBean(poet.id, "登幽州台歌", "前不见古人，后不见来者"));
			poemService.deletePoet(poet.id);
		}
	}

	@Test
	public void testDeletePoetOK() {
		try (UserContext context = new UserContext(super.editorUser)) {
			Poet poet = poemService.createPoet(newPoetBean(getTangDynasty().id, "陈子昂"));
			poemService.deletePoet(poet.id);
		}
	}

	// poem ///////////////////////////////////////////////////////////////////

	@Test(expected = EntityNotFoundException.class)
	public void testGetPoemButNotFound() {
		poemService.getPoem(IdUtils.next());
	}

	@Test(expected = APIPermissionException.class)
	public void testCreatePoemFailedWithoutPermission() {
		try (UserContext context = new UserContext(super.editorUser)) {
			poemService.createPoet(newPoetBean(getTangDynasty().id, "陈子昂"));
		}
		try (UserContext context = new UserContext(super.normalUser)) {
			poemService.createPoem(null);
		}
	}

	@Test(expected = APIArgumentException.class)
	public void testCreatePoemFailedWithBadName() {
		try (UserContext context = new UserContext(super.editorUser)) {
			Poet poet = poemService.createPoet(newPoetBean(getTangDynasty().id, "陈子昂"));
			poemService.createPoem(newPoemBean(poet.id, "   \u3000  ", "前不见古人，后不见来者"));
		}
	}

	@Test(expected = APIArgumentException.class)
	public void testCreatePoemFailedWithBadContent() {
		try (UserContext context = new UserContext(super.editorUser)) {
			Poet poet = poemService.createPoet(newPoetBean(getTangDynasty().id, "陈子昂"));
			poemService.createPoem(newPoemBean(poet.id, "登幽州台歌", "  \n \t  \u3000  "));
		}
	}

	@Test(expected = APIArgumentException.class)
	public void testCreatePoemFailedWithBadForm() {
		try (UserContext context = new UserContext(super.editorUser)) {
			Poet poet = poemService.createPoet(newPoetBean(getTangDynasty().id, "陈子昂"));
			PoemBean bean = newPoemBean(poet.id, "登幽州台歌", "前不见古人，后不见来者");
			bean.form = 1234;
			poemService.createPoem(bean);
		}
	}

	@Test(expected = EntityNotFoundException.class)
	public void testCreatePoemFailedWithBadPoetId() {
		try (UserContext context = new UserContext(super.editorUser)) {
			poemService.createPoem(newPoemBean(IdUtils.next(), "登幽州台歌", "前不见古人，后不见来者"));
		}
	}

	@Test
	public void testCreatePoemWithoutImageAndGetOK() {
		Poet poet = null;
		Poem poem = null;
		try (UserContext context = new UserContext(super.editorUser)) {
			poet = poemService.createPoet(newPoetBean(getTangDynasty().id, "陈子昂"));
			PoemBean bean = newPoemBean(poet.id, " 登●幽 州 臺[1]歌", "前不(2)見 古人，\n后不见③来者 \t\n  \u3000");
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
		assertEquals(1, poemService.getPoet(poet.id).poemCount);
	}

	@Test
	public void testCreatePoemWithImageAndGetOK() throws IOException {
		Poet poet = null;
		Poem poem = null;
		try (UserContext context = new UserContext(super.editorUser)) {
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
		try (UserContext context = new UserContext(super.editorUser)) {
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
		try (UserContext context = new UserContext(super.normalUser)) {
			poemService.updatePoem(null, null);
		}
	}

	@Test(expected = APIArgumentException.class)
	public void testUpdatePoemFailedWithBadName() {
		Poet poet = null;
		Poem poem = null;
		try (UserContext context = new UserContext(super.editorUser)) {
			poet = poemService.createPoet(newPoetBean(getTangDynasty().id, "陈子昂"));
			poem = poemService.createPoem(newPoemBean(poet.id, "登幽州台歌", "前不见古人，后不见来者"));
			poemService.updatePoem(poem.id, newPoemBean(poet.id, "⒈⒉⒊ \n ", "前不见古人，后不见来者"));
		}
	}

	@Test(expected = APIArgumentException.class)
	public void testUpdatePoemFailedWithBadContent() {
		Poet poet = null;
		Poem poem = null;
		try (UserContext context = new UserContext(super.editorUser)) {
			poet = poemService.createPoet(newPoetBean(getTangDynasty().id, "陈子昂"));
			poem = poemService.createPoem(newPoemBean(poet.id, "登幽州台歌", "前不见古人，后不见来者"));
			poemService.updatePoem(poem.id, newPoemBean(poet.id, "登幽州台歌", " \n \u3000 \t\t\r\n(1)\n "));
		}
	}

	@Test(expected = APIArgumentException.class)
	public void testUpdatePoemFailedWithBadForm() {
		Poet poet = null;
		Poem poem = null;
		try (UserContext context = new UserContext(super.editorUser)) {
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
		try (UserContext context = new UserContext(super.editorUser)) {
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
		try (UserContext context = new UserContext(super.editorUser)) {
			poet = poemService.createPoet(newPoetBean(getTangDynasty().id, "陈子昂"));
			poem = poemService.createPoem(newPoemBean(poet.id, "登幽州台歌", "前不见古人，后不见来者"));
			poemService.updatePoem(poem.id, newPoemBean(IdUtils.next(), "幽州台歌", "前不见，后不见"));
		}
	}

	@Test
	public void testUpdatePoemOK() {
		Poet poet = null;
		Poem poem = null;
		try (UserContext context = new UserContext(super.editorUser)) {
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
		assertEquals(1, poemService.getPoet(poet.id).poemCount);
	}

	@Test
	public void testUpdatePoemOKWithImageAdded() throws IOException {
		Poet poet = null;
		Poem poem = null;
		try (UserContext context = new UserContext(super.editorUser)) {
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
		try (UserContext context = new UserContext(super.editorUser)) {
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
		try (UserContext context = new UserContext(super.editorUser)) {
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
		try (UserContext context = new UserContext(super.editorUser)) {
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
		try (UserContext context = new UserContext(super.normalUser)) {
			poemService.deletePoem(null);
		}
	}

	// featured ///////////////////////////////////////////////////////////////

	@Test(expected = APIPermissionException.class)
	public void testSetAsFeaturedFailedWithoutPermission() {
		try (UserContext context = new UserContext(super.normalUser)) {
			poemService.setPoemAsFeatured(null);
		}
	}

	@Test(expected = APIArgumentException.class)
	public void testSetAsFeaturedFailedWithDateTooSmall() {
		try (UserContext context = new UserContext(super.editorUser)) {
			Poet poet = poemService.createPoet(newPoetBean(getTangDynasty().id, "陈子昂"));
			Poem poem = poemService.createPoem(newPoemBean(poet.id, "登幽州台歌", "前不见古人，后不见来者"));
			poemService.setPoemAsFeatured(newFeaturedBean(poem.id, LocalDate.of(1999, 12, 31)));
		}
	}

	@Test(expected = APIArgumentException.class)
	public void testSetAsFeaturedFailedWithDateTooLarge() {
		try (UserContext context = new UserContext(super.editorUser)) {
			Poet poet = poemService.createPoet(newPoetBean(getTangDynasty().id, "陈子昂"));
			Poem poem = poemService.createPoem(newPoemBean(poet.id, "登幽州台歌", "前不见古人，后不见来者"));
			poemService.setPoemAsFeatured(newFeaturedBean(poem.id, LocalDate.of(2100, 1, 1)));
		}
	}

	@Test(expected = EntityNotFoundException.class)
	public void testSetAsFeaturedFailedWithPoemNotFound() {
		try (UserContext context = new UserContext(super.editorUser)) {
			poemService.setPoemAsFeatured(newFeaturedBean(IdUtils.next(), LocalDate.of(2016, 1, 1)));
		}
	}

	@Test(expected = APIArgumentException.class)
	public void testSetAsFeaturedFailedForPoemHasNoImage() {
		try (UserContext context = new UserContext(super.editorUser)) {
			Poet poet = poemService.createPoet(newPoetBean(getTangDynasty().id, "陈子昂"));
			Poem poem = poemService.createPoem(newPoemBean(poet.id, "登幽州台歌", "前不见古人，后不见来者"));
			poemService.setPoemAsFeatured(newFeaturedBean(poem.id, LocalDate.of(2016, 1, 1)));
		}
	}

	@Test
	public void testSetAsFeaturedOK() throws IOException {
		try (UserContext context = new UserContext(super.editorUser)) {
			Poet poet = poemService.createPoet(newPoetBean(getTangDynasty().id, "陈子昂"));
			PoemBean poemBean = newPoemBean(poet.id, "登幽州台歌", "前不见古人，后不见来者");
			poemBean.imageData = Base64Util.encodeToString(FileUtil.getResource("/640x360.jpg"));
			Poem poem = poemService.createPoem(poemBean);
			poemService.setPoemAsFeatured(newFeaturedBean(poem.id, LocalDate.of(2016, 1, 1)));
		}
	}

	@Test(expected = APIArgumentException.class)
	public void testSetAsFeaturedFailedForPoemAlreadyFeatured() throws IOException {
		try (UserContext context = new UserContext(super.editorUser)) {
			Poet poet = poemService.createPoet(newPoetBean(getTangDynasty().id, "陈子昂"));
			PoemBean poemBean = newPoemBean(poet.id, "登幽州台歌", "前不见古人，后不见来者");
			poemBean.imageData = Base64Util.encodeToString(FileUtil.getResource("/640x360.jpg"));
			Poem poem = poemService.createPoem(poemBean);
			poemService.setPoemAsFeatured(newFeaturedBean(poem.id, LocalDate.of(2016, 1, 1)));
			poemService.setPoemAsFeatured(newFeaturedBean(poem.id, LocalDate.of(2016, 2, 2)));
		}
	}

	@Test(expected = DuplicateKeyException.class)
	public void testSetAsFeaturedFailedForPubDateAlreadyFeatured() throws IOException {
		try (UserContext context = new UserContext(super.editorUser)) {
			Poet poet = poemService.createPoet(newPoetBean(getTangDynasty().id, "陈子昂"));
			PoemBean poemBean1 = newPoemBean(poet.id, "登幽州台歌", "前不见古人，后不见来者");
			poemBean1.imageData = Base64Util.encodeToString(FileUtil.getResource("/640x360.jpg"));
			Poem poem1 = poemService.createPoem(poemBean1);
			PoemBean poemBean2 = newPoemBean(poet.id, "送客", "故人洞庭去，杨柳春风生。");
			poemBean2.imageData = Base64Util.encodeToString(FileUtil.getResource("/640x360.jpg"));
			Poem poem2 = poemService.createPoem(poemBean2);
			poemService.setPoemAsFeatured(newFeaturedBean(poem1.id, LocalDate.of(2016, 1, 1)));
			poemService.setPoemAsFeatured(newFeaturedBean(poem2.id, LocalDate.of(2016, 1, 1)));
		}
	}

	@Test(expected = APIPermissionException.class)
	public void testSetAsUnfeaturedFailedWithoutPermission() {
		try (UserContext context = new UserContext(super.normalUser)) {
			poemService.setPoemAsUnfeatured(null);
		}
	}

	@Test(expected = EntityNotFoundException.class)
	public void testSetAsUnfeaturedFailedForPoemNotExist() {
		try (UserContext context = new UserContext(super.editorUser)) {
			poemService.setPoemAsUnfeatured(IdUtils.next());
		}
	}

	@Test
	public void testSetAsUnfeaturedOK() throws IOException {
		try (UserContext context = new UserContext(super.editorUser)) {
			Poet poet = poemService.createPoet(newPoetBean(getTangDynasty().id, "陈子昂"));
			PoemBean poemBean = newPoemBean(poet.id, "登幽州台歌", "前不见古人，后不见来者");
			poemBean.imageData = Base64Util.encodeToString(FileUtil.getResource("/640x360.jpg"));
			Poem poem = poemService.createPoem(poemBean);
			poemService.setPoemAsFeatured(newFeaturedBean(poem.id, LocalDate.of(2016, 1, 1)));
			poemService.setPoemAsUnfeatured(poem.id);
		}
	}

	@Test
	public void testGetFeaturedPoemsAndPoem() throws IOException {
		try (UserContext context = new UserContext(super.editorUser)) {
			Poet poet = poemService.createPoet(newPoetBean(getTangDynasty().id, "陈子昂"));
			PoemBean poemBean1 = newPoemBean(poet.id, "登幽州台歌", "前不见古人，后不见来者");
			PoemBean poemBean2 = newPoemBean(poet.id, "送客", "故人洞庭去，杨柳春风生。");
			poemBean1.imageData = poemBean2.imageData = Base64Util.encodeToString(FileUtil.getResource("/640x360.jpg"));
			Poem poem1 = poemService.createPoem(poemBean1);
			Poem poem2 = poemService.createPoem(poemBean2);
			poemService.setPoemAsFeatured(newFeaturedBean(poem1.id, LocalDate.of(2016, 1, 1)));
			poemService.setPoemAsFeatured(newFeaturedBean(poem2.id, LocalDate.of(2016, 2, 2)));
			List<TheFeaturedPoem> featured = poemService.getFeaturedPoems();
			assertNotNull(featured);
			assertEquals(2, featured.size());
			assertEquals(poem2.name, featured.get(0).poem.name);
			assertEquals(poem1.name, featured.get(1).poem.name);
			assertEquals(LocalDate.of(2016, 2, 2), featured.get(0).pubDate);
			assertEquals(LocalDate.of(2016, 1, 1), featured.get(1).pubDate);
			// get single:
			Poem p11 = poemService.getFeaturedPoem(LocalDate.of(2016, 1, 1));
			assertEquals(poem1.id, p11.id);
			Poem p12 = poemService.getFeaturedPoem(LocalDate.of(2016, 1, 2));
			assertEquals(poem1.id, p12.id);
			Poem p21 = poemService.getFeaturedPoem(LocalDate.of(2016, 2, 1));
			assertEquals(poem1.id, p21.id);
			Poem p22 = poemService.getFeaturedPoem(LocalDate.of(2016, 2, 2));
			assertEquals(poem2.id, p22.id);
			Poem p33 = poemService.getFeaturedPoem(LocalDate.of(2016, 3, 3));
			assertEquals(poem2.id, p33.id);
		}
	}

	// category ///////////////////////////////////////////////////////////////

	@Test(expected = APIPermissionException.class)
	public void testCreateCategoryFailedWithoutPermission() {
		try (UserContext ctx = new UserContext(this.normalUser)) {
			poemService.createCategory(null);
		}
	}

	@Test(expected = APIArgumentException.class)
	public void testCreateCategoryFailedForBadName() {
		try (UserContext ctx = new UserContext(this.editorUser)) {
			poemService.createCategory(newCategoryBean("  \u3000 \r\n "));
		}
	}

	@Test
	public void testCreateCategoryOK() {
		try (UserContext ctx = new UserContext(this.editorUser)) {
			Category cat = poemService.createCategory(newCategoryBean("唐诗三百首"));
			assertEquals("唐诗三百首", cat.name);
			assertEquals("唐詩三百首", cat.nameCht);
			assertEquals("简介：唐诗三百首", cat.description);
			assertEquals("简介：唐詩三百首", cat.descriptionCht);
			List<TheCategoryPoem> list = poemService.getPoemsOfCategory(cat.id);
			assertTrue(list.isEmpty());
		}
	}

	@Test(expected = EntityNotFoundException.class)
	public void testGetCategoryFailedForNotExist() {
		poemService.getPoemsOfCategory(IdUtils.next());
	}

	@Test
	public void testUpdatePoemsOfCategoryAndGetOK() {
		try (UserContext ctx = new UserContext(this.editorUser)) {
			Poet poet1 = poemService.createPoet(newPoetBean(getTangDynasty().id, "李白"));
			Poet poet2 = poemService.createPoet(newPoetBean(getTangDynasty().id, "陈子昂"));
			Poem poem1a = poemService.createPoem(newPoemBean(poet1.id, "赠汪伦", "李白乘舟将欲行，忽闻岸上踏歌声。"));
			Poem poem1b = poemService.createPoem(newPoemBean(poet1.id, "送孟浩然之广陵", "故人西辞黄鹤楼，烟花三月下扬州。"));
			Poem poem2a = poemService.createPoem(newPoemBean(poet2.id, "登幽州台歌", "前不见古人，后不见来者"));
			Poem poem2b = poemService.createPoem(newPoemBean(poet2.id, "送客", "故人洞庭去，杨柳春风生。"));
			Category cat = poemService.createCategory(newCategoryBean("唐诗三百首"));
			poemService.updatePoemsOfCategory(cat.id, Arrays.asList(newCategoryPoemBean("七律", poem1a.id, poem1b.id),
					newCategoryPoemBean("五律", poem2a.id, poem2b.id)));
			// get:
			List<TheCategoryPoem> list = poemService.getPoemsOfCategory(cat.id);
			assertEquals(2, list.size());
			TheCategoryPoem sec1 = list.get(0);
			assertEquals("七律", sec1.sectionName);
			assertEquals("赠汪伦", sec1.poems.get(0).name);
			assertEquals("送孟浩然之广陵", sec1.poems.get(1).name);
			TheCategoryPoem sec2 = list.get(1);
			assertEquals("五律", sec2.sectionName);
			assertEquals("登幽州台歌", sec2.poems.get(0).name);
			assertEquals("送客", sec2.poems.get(1).name);
		}
	}

	@Test(expected = APIPermissionException.class)
	public void testUpdateCategoryFailedWithoutPermission() {
		try (UserContext ctx = new UserContext(this.normalUser)) {
			poemService.updateCategory(null, null);
		}
	}

	@Test(expected = APIPermissionException.class)
	public void testUpdatePoemsOfCategoryFailedWithoutPermission() {
		try (UserContext ctx = new UserContext(this.normalUser)) {
			poemService.updatePoemsOfCategory(null, null);
		}
	}

	@Test(expected = APIPermissionException.class)
	public void testDeleteCategoryFailedWithoutPermission() {
		try (UserContext ctx = new UserContext(this.normalUser)) {
			poemService.deleteCategory(null);
		}
	}

	@Test(expected = APIEntityConflictException.class)
	public void testDeleteNonEmptyCategoryFailed() {
		try (UserContext ctx = new UserContext(this.editorUser)) {
			Poet poet1 = poemService.createPoet(newPoetBean(getTangDynasty().id, "李白"));
			Poem poem1a = poemService.createPoem(newPoemBean(poet1.id, "赠汪伦", "李白乘舟将欲行，忽闻岸上踏歌声。"));
			Poem poem1b = poemService.createPoem(newPoemBean(poet1.id, "送孟浩然之广陵", "故人西辞黄鹤楼，烟花三月下扬州。"));
			Category cat = poemService.createCategory(newCategoryBean("唐诗三百首"));
			poemService.updatePoemsOfCategory(cat.id, Arrays.asList(newCategoryPoemBean("七律", poem1a.id, poem1b.id)));
			// delete:
			poemService.deleteCategory(cat.id);
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
