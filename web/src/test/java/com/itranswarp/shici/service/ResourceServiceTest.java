package com.itranswarp.shici.service;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.itranswarp.shici.model.Poem;
import com.itranswarp.shici.model.Resource;
import com.itranswarp.shici.model.User;
import com.itranswarp.shici.store.AliyunStorageTest;
import com.itranswarp.shici.util.FileUtil;
import com.itranswarp.warpdb.IdUtil;
import com.itranswarp.warpdb.context.UserContext;
import com.itranswarp.warpdb.entity.BaseEntity;

public class ResourceServiceTest extends AbstractServiceTestBase {

	ResourceService resourceService;

	@Before
	public void setUp() throws Exception {
		resourceService = initResourceService();
	}

	public ResourceService initResourceService() {
		ResourceService s = new ResourceService();
		s.database = super.database;
		s.storage = new AliyunStorageTest().initAliyunStorage();
		return s;
	}

	@Test
	public void testCreateResourceAsImage() throws IOException {
		BaseEntity be = new Poem();
		be.id = IdUtil.next();
		try (UserContext<User> ctx = new UserContext<User>(this.editorUser)) {
			Resource res = resourceService.createResource(be, "cover", ".jpg", FileUtil.getResource("/1280x800.jpg"));
			assertEquals(be.id, res.refId);
			assertEquals("Poem", res.refType);
			assertEquals("cover", res.name);
			assertEquals("image/jpeg", res.mime);
			assertEquals(126860, res.size);
			assertTrue(res.url.startsWith("//shici-test.oss-cn-hangzhou.aliyuncs.com/"));
			assertTrue(res.internalUrl.startsWith("//shici-test.oss-cn-hangzhou-internal.aliyuncs.com/"));
			assertTrue(res.largeImageUrl.startsWith("//shici-test.img-cn-hangzhou.aliyuncs.com/"));
			assertTrue(res.mediumImageUrl.startsWith("//shici-test.img-cn-hangzhou.aliyuncs.com/"));
			assertTrue(res.smallImageUrl.startsWith("//shici-test.img-cn-hangzhou.aliyuncs.com/"));
		}
	}

	@Test
	public void testCreateResourceAsBinary() throws IOException {
		BaseEntity be = new Poem();
		be.id = IdUtil.next();
		try (UserContext<User> ctx = new UserContext<User>(this.editorUser)) {
			Resource res = resourceService.createResource(be, "readme", ".txt", FileUtil.getResource("/license.txt"));
			assertEquals(be.id, res.refId);
			assertEquals("Poem", res.refType);
			assertEquals("readme", res.name);
			assertEquals("text/plain", res.mime);
			assertEquals(11357, res.size);
			assertTrue(res.url.startsWith("//shici-test.oss-cn-hangzhou.aliyuncs.com/"));
			assertTrue(res.internalUrl.startsWith("//shici-test.oss-cn-hangzhou-internal.aliyuncs.com/"));
			assertEquals("", res.largeImageUrl);
			assertEquals("", res.mediumImageUrl);
			assertEquals("", res.smallImageUrl);
		}
	}

}
