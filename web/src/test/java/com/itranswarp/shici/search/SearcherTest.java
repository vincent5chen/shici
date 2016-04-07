package com.itranswarp.shici.search;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class SearcherTest {

	final String INDEX = "thetestindex";

	Searcher searcher;

	@Before
	public void setUp() throws Exception {
		searcher = new Searcher();
		searcher.esUrl = "http://localhost:9200/";
		if (searcher.indexExist(INDEX)) {
			searcher.deleteIndex(INDEX);
		}
		searcher.createIndex(INDEX);
		searcher.createMapping(Tweet.class);
	}

	@Test
	public void testIndexExist() {
		assertFalse(searcher.indexExist("notexist"));
		assertTrue(searcher.indexExist(INDEX));
	}

	@Test
	public void testCreateAndDeleteIndex() {
		String TEMP_NAME = "thetmpindex";
		searcher.createIndex(TEMP_NAME);
		assertTrue(searcher.indexExist(TEMP_NAME));
		searcher.deleteIndex(TEMP_NAME);
		assertFalse(searcher.indexExist(TEMP_NAME));
	}

	@Test
	public void testCreateDocAndGetAndDelete() {
		Tweet t = Tweet.newTweet("michael", "Hello", "Hello, from 北京奥林匹克森林公园！");
		searcher.createDocument(INDEX, t);
		// test
		Tweet gt = searcher.getDocument(INDEX, Tweet.class, t.id);
		assertNotNull(gt);
		assertEquals(t.id, gt.id);
		assertEquals(t.username, gt.username);
		assertEquals(t.message, gt.message);
		// delete:
		searcher.deleteDocument(INDEX, Tweet.class, t.id);
		// try get:
		try {
			searcher.getDocument(INDEX, Tweet.class, t.id);
			fail("Not deleted.");
		} catch (SearchResultException e) {
		}
	}

	@Test
	public void testCreateDocAndCreateAgain() {
		Tweet t1 = Tweet.newTweet("michael", "Hello", "Hello, from 北京奥林匹克森林公园！");
		searcher.createDocument(INDEX, t1);
		Tweet t2 = Tweet.newTweet("tracy", "Moring", "Good morning！");
		t2.id = t1.id;
		searcher.createDocument(INDEX, t2);
		// test
		Tweet gt = searcher.getDocument(INDEX, Tweet.class, t1.id);
		assertNotNull(gt);
		assertEquals(t2.id, gt.id);
		assertEquals(t2.username, gt.username);
		assertEquals(t2.message, gt.message);
	}

	@Test
	public void testSearchEmpty() {
		String[][] texts = { { "王二", "全栈设计师", "一名全栈设计师的Mac工具箱 (附软件列表)" },
				{ "王小二", "AR才是终极目标", "谷歌说:让Facebook做VR吧，AR才是终极目标！" },
				{ "Zoe曾几", "三衢道中", "梅子黄时日日晴，小溪泛尽却山行。绿阴不减来时路，添得黄鹂四五声。" },
				{ "Miss Lee", "江畔独步寻花", "黄四娘家花满蹊，千朵万朵压枝低。留连戏蝶时时舞，自在娇莺恰恰啼。" },
				{ "Apple Inc.", "谷歌挖角苹果", "谷歌最近将苹果前全球供应链经理招致麾下，帮助该公司管理无人驾驶汽车的供应链。" },
				{ "微软MSDN", "微软与NASA合作", "微软与NASA合作：用HoloLens体验登陆火星" },
				{ "103.9", "新能源车", "全球新能源车销量激增，国内电动汽车在政策扶持销量持续攀升" },
				{ "Robot Wall-E", "3D打印美女机器人", "3D打印美女机器人酷似斯嘉丽\u00b7约翰逊" },
				{ "Dead Pool", "他拥有超越人类的力量", "他拥有超越人类的力量、耐力、速度及自我愈合能力，精通各种武器，擅长搏击术，不过他的精神不太稳定。" },
				{ "Iron Man", "绝句", "迟日江山丽，春风花草香。泥融飞燕子，沙暖睡鸳鸯。" } };
		String lastId = null;
		for (String[] txt : texts) {
			Tweet t = Tweet.newTweet(txt[0], txt[1], txt[2]);
			lastId = t.id;
			searcher.createDocument(INDEX, t);
		}
		Tweet last = Tweet.newTweet("Iron钢铁侠", "埃隆·马斯克", "硅谷钢铁侠：埃隆·马斯克的冒险人生");
		last.id = lastId;
		searcher.createDocument(INDEX, last);
		// search:
		// searcher.search(INDEX, q, pageIndex)
	}
}
