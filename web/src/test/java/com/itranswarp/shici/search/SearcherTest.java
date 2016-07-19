package com.itranswarp.shici.search;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
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
	public void testSearch() throws Exception {
		String[][] texts = { { "李白", "赠汪伦", "李白乘舟将欲行，忽闻岸上踏歌声。桃花潭水深千尺，不及汪伦送我情。" },
				{ "李端", "听筝", "鸣筝金粟柱，素手玉房前。欲得周郎顾，时时误拂弦。" },
				{ "杜甫", "天末怀李白", "凉风起天末，君子意如何。鸿雁几时到，江湖秋水多。文章憎命达，魑魅喜人过。应共冤魂语，投诗赠汨罗。" },
				{ "曾几", "三衢道中", "梅子黄时日日晴，小溪泛尽却山行。绿阴不减来时路，添得黄鹂四五声。" },
				{ "杜甫", "江畔独步寻花", "黄四娘家花满蹊，千朵万朵压枝低。留连戏蝶时时舞，自在娇莺恰恰啼。" },
				{ "李白", "望天门山", "天门中断楚江开，碧水东流至此回。两岸青山相对出，孤帆一片日边来。" },
				{ "李白", "望庐山瀑布", "日照香炉生紫烟，遥看瀑布挂前川。飞流直下三千尺，疑是银河落九天。" },
				{ "杜牧", "赤壁", "折戟沉沙铁未销，自将磨洗认前朝。东风不与周郎便，铜雀春深锁二乔。" },
				{ "苏轼", "念奴娇·赤壁怀古", "大江东去，浪淘尽，千古风流人物。故垒西边，人道是，三国周郎赤壁。乱石穿空，惊涛拍岸，卷起千堆雪。" },
				{ "杜甫", "绝句", "迟日江山丽，春风花草香。泥融飞燕子，沙暖睡鸳鸯。" },
				{ "白居易", "赋得古原草送别", "离离原上草，一岁一枯荣。野火烧不尽，春风吹又生。远芳侵古道，晴翠接荒城。又送王孙去，萋萋满别情。" },
				{ "程准", "水调歌头", "船系钓台下，身寄碧云端。胸中千古风月，笔下助波澜。" } };
		String lastId = null;
		for (String[] txt : texts) {
			Tweet t = Tweet.newTweet(txt[0], txt[1], txt[2]);
			lastId = t.id;
			searcher.createDocument(INDEX, t);
		}
		Thread.sleep(2000);
		// search by term:
		final String LEE = "李";
		List<Tweet> results = searcher.search(INDEX, Tweet.class, new String[] { LEE }, 10);
		assertFalse(results.isEmpty());
		for (Tweet tweet : results) {
			assertTrue(tweet.username.contains(LEE) || tweet.title.contains(LEE) || tweet.message.contains(LEE));
		}
		// search by xx:
		final String LIBAI = "李白";
		results = searcher.search(INDEX, Tweet.class, new String[] { LIBAI }, 10);
		assertFalse(results.isEmpty());
		for (Tweet tweet : results) {
			assertTrue(tweet.username.contains(LIBAI) || tweet.title.contains(LIBAI) || tweet.message.contains(LIBAI));
		}
		// search by xxxxx:
		final String RZXLSZY = "日照香炉生紫烟";
		results = searcher.search(INDEX, Tweet.class, new String[] { RZXLSZY }, 10);
		assertFalse(results.isEmpty());
		for (Tweet tweet : results) {
			assertTrue(tweet.message.contains(RZXLSZY));
		}
		// search by xxxxx with fuzzy:
		final String RZXLSZY2 = "日照香炉升紫烟";
		results = searcher.search(INDEX, Tweet.class, new String[] { RZXLSZY2 }, 10);
		assertFalse(results.isEmpty());
		for (Tweet tweet : results) {
			assertTrue(tweet.message.contains(RZXLSZY));
		}
		// search by xxxx with fuzzy:
		final String QGFL = "千古风流";
		results = searcher.search(INDEX, Tweet.class, new String[] { QGFL }, 10);
		assertEquals(2, results.size());
		assertTrue(results.get(0).message.contains(QGFL));
		assertTrue(results.get(1).message.contains("千古风月"));
		// search by xx and xx:
		results = searcher.search(INDEX, Tweet.class, new String[] { LIBAI, QGFL }, 10);
		assertFalse(results.isEmpty());
		for (Tweet tweet : results) {
			assertTrue(tweet.message.contains(QGFL) || tweet.message.contains("千古风月") || tweet.message.contains("LIBAI")
					|| tweet.username.contains(LIBAI) || tweet.title.contains(LIBAI));
		}
		// search "船系钓台下":
		final String CXDTX = "船系钓台下";
		results = searcher.search(INDEX, Tweet.class, new String[] { CXDTX }, 10);
		assertEquals(1, results.size());
		// delete doc:
		searcher.deleteDocument(INDEX, Tweet.class, lastId);
		Thread.sleep(2000);
		// search again should not found:
		results = searcher.search(INDEX, Tweet.class, new String[] { CXDTX }, 10);
		assertTrue(results.isEmpty());
	}
}
