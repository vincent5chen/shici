package com.itranswarp.shici.store;

import static org.junit.Assert.*;

import java.io.IOException;
import java.time.LocalDateTime;

import org.junit.Before;
import org.junit.Test;

import com.itranswarp.shici.TestHelper;
import com.itranswarp.shici.util.FileUtil;

public class AliyunStorageTest {

	AbstractStorage storage;

	@Before
	public void setUp() throws Exception {
		storage = initAliyunStorage();
	}

	public AbstractStorage initAliyunStorage() {
		AliyunStorage as = new AliyunStorage();
		as.location = TestHelper.getProperty("storage.resource.location");
		as.host = TestHelper.getProperty("storage.resource.host");
		as.internalHost = TestHelper.getProperty("storage.resource.internal.host");
		as.bucket = TestHelper.getProperty("storage.resource.bucket");
		as.accessKeyId = TestHelper.getProperty("storage.resource.access_key_id");
		as.accessKeySecret = TestHelper.getProperty("storage.resource.access_key_secret");
		return as;
	}

	@Test
	public void testPutAsInputStream() throws IOException {
		RemoteObject ro = storage.put(".jpg", FileUtil.getResource("/1280x800.jpg"));
		assertEquals("shici-test", ro.bucket);
		assertEquals("cn-hangzhou", ro.location);
		assertTrue(ro.object.startsWith(LocalDateTime.now().format(AbstractStorage.FORMATTER)));
		System.out.println(storage.toUrl(ro));
		System.out.println(storage.toInternalUrl(ro));
		System.out.println(storage.toImageUrl(ro, "100w_100h.jpg"));
	}

}
