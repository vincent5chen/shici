package com.itranswarp.shici.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import com.itranswarp.shici.bean.Dynasty;
import com.itranswarp.shici.bean.Poem;
import com.itranswarp.shici.bean.Poet;
import com.itranswarp.shici.util.Utils;

/**
 * Poem service which holds all poem data.
 * 
 * @author liaoxuefeng
 */
@Component
public class PoemService {

	final Logger logger = LoggerFactory.getLogger(getClass());

	Map<Long, Dynasty> dynasties = new HashMap<>();
	Map<Long, Poet> poets = new HashMap<>();
	Map<Long, Poem> poems = new HashMap<>();

	List<Dynasty> cachedDynasties;
	Map<Long, List<Poet>> cachedPoets = new ConcurrentHashMap<>();
	Map<Long, List<Poem>> cachedPoems = new ConcurrentHashMap<>();

	Map<String, Poem> featuredPoems = new ConcurrentHashMap<>();

	public Poem getFeatured() {
		ZonedDateTime now = ZonedDateTime.now();
		int offset = now.getOffset().getTotalSeconds();
		long ts = now.toEpochSecond() + offset;
		String key = "featured-" + ts / (3600 * 24);
		Poem featured = this.featuredPoems.get(key);
		if (featured == null) {
			int hash = Integer.MAX_VALUE & Utils.hashAsInt(key);
			int index = hash % poems.size();
			Iterator<Poem> it = poems.values().iterator();
			for (int i = 0; i < index; i++) {
				it.next();
			}
			featured = it.next();
			this.featuredPoems.put(key, featured);
		}
		return featured;
	}

	public List<Dynasty> getDynasties() {
		if (this.cachedDynasties == null) {
			this.cachedDynasties = dynasties.values().stream().sorted().collect(Collectors.toList());
		}
		return this.cachedDynasties;
	}

	public Dynasty getDynasty(long id) {
		return checkNonNull(this.dynasties.get(id));
	}

	public List<Poet> getPoets(long dynastyId) {
		List<Poet> list = this.cachedPoets.get(dynastyId);
		if (list == null) {
			list = this.poets.values().stream().filter(poet -> poet.getDynasty().getId() == dynastyId).sorted()
					.collect(Collectors.toList());
			this.cachedPoets.put(dynastyId, list);
		}
		return list;
	}

	public Poet getPoet(long id) {
		return checkNonNull(this.poets.get(id));
	}

	public List<Poem> getPoems(long poetId) {
		List<Poem> list = this.cachedPoems.get(poetId);
		if (list == null) {
			list = this.poems.values().stream().filter(poem -> poem.getPoet().getId() == poetId).sorted()
					.collect(Collectors.toList());
			this.cachedPoems.put(poetId, list);
		}
		return list;
	}

	public Poem getPoem(long id) {
		return checkNonNull(this.poems.get(id));
	}

	<T> T checkNonNull(T obj) {
		if (obj == null) {
			throw new IllegalArgumentException("null object");
		}
		return obj;
	}

	@PostConstruct
	public void init() throws IOException {
		Resource[] resources = Utils.loadResources("classpath*:text/*/*/*.txt");
		Arrays.sort(resources, (r1, r2) -> {
			try {
				String s1 = r1.getURL().toString();
				String s2 = r2.getURL().toString();
				boolean m1 = s1.endsWith("/meta.txt");
				boolean m2 = s2.endsWith("/meta.txt");
				if (m1 && m2) {
					return s1.compareTo(s2);
				}
				if (m1) {
					return -1;
				}
				if (m2) {
					return 1;
				}
				return s1.compareTo(s2);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
		logger.info("loaded {} resources.", resources.length);
		Pattern pattern = Pattern.compile("^.*\\/text\\/(\\d+)\\.(.*)\\/(.*)\\/(.*)\\.txt$");
		for (Resource resource : resources) {
			String url = resource.getURL().toString();
			Matcher matcher = pattern.matcher(url);
			if (matcher.matches()) {
				long dynastyId = Long.parseLong(matcher.group(1)) << 56;
				String dynastyName = matcher.group(2);
				String poetName = matcher.group(3);
				String poemName = matcher.group(4);
				Dynasty dynasty = createDynasty(dynastyId, dynastyName);
				Poet poet = createPoet(dynasty, poetName, resource);
				if (!poemName.equals("meta")) {
					createPoem(poet, poemName, resource);
				}
			} else {
				logger.warn("could not parse resource: {}", url);
			}
		}
		logger.info("loaded {} dynasties, {} poets, {} poems.", this.dynasties.size(), this.poets.size(),
				this.poems.size());
	}

	public Poem createPoem(Poet poet, String poemName, Resource resource) throws IOException {
		int hash = Utils.hashAsInt(poemName);
		long poemId = poet.getId() | (hash & 0xffffffffL);
		Poem poem = poems.get(poemId);
		if (poem != null && !poem.getName().equals(poemName)) {
			throw new RuntimeException("Hash collision for poem: " + poemName + " with exist: " + poem.getName());
		}
		if (poem == null) {
			Map<String, String> meta = readFile(resource, false);
			poem = new Poem(poet, poemId, meta.getOrDefault("form", "未知"), meta.getOrDefault("tags", "").split(","),
					poemName, meta.get("content"));
			poems.put(poemId, poem);
			if (logger.isDebugEnabled()) {
				logger.info("new poem: {}", poem);
			}
		}
		return poem;
	}

	public Poet createPoet(Dynasty dynasty, String poetName, Resource resource) throws IOException {
		int hash = Utils.hashAsInt(poetName);
		long poetId = dynasty.getId() | ((hash & 0x00ffffffL) << 32);
		Poet poet = poets.get(poetId);
		if (poet != null && !poet.getName().equals(poetName)) {
			throw new RuntimeException("Hash collision for poet: " + poetName + " with exist: " + poet.getName());
		}
		if (poet == null) {
			Map<String, String> meta = readFile(resource, true);
			poet = new Poet(dynasty, poetId, poetName, meta.get("content"), meta.getOrDefault("birth", ""),
					meta.getOrDefault("death", ""));
			poets.put(poetId, poet);
			if (logger.isDebugEnabled()) {
				logger.debug("new poet: {}", poet);
			}
		}
		return poet;
	}

	public Dynasty createDynasty(long dynastyId, String dynastyName) {
		Dynasty dynasty = dynasties.get(dynastyId);
		if (dynasty != null && !dynasty.getName().equals(dynastyName)) {
			throw new RuntimeException("Hash collision for dynasty: " + dynasty);
		}
		if (dynasty == null) {
			dynasty = new Dynasty(dynastyId, dynastyName);
			dynasties.put(dynastyId, dynasty);
			logger.info("new dynasty: {}", dynasty);
		}
		return dynasty;
	}

	Map<String, String> readFile(Resource resource, boolean singleLine) throws IOException {
		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
			Map<String, String> map = new HashMap<>();
			boolean readMeta = true;
			StringBuilder content = new StringBuilder(1024);
			for (;;) {
				String line = reader.readLine();
				if (line == null) {
					break;
				}
				if (readMeta) {
					Matcher matcher = PATTERN_META.matcher(line);
					if (matcher.matches()) {
						map.put(matcher.group(1), matcher.group(2));
					} else {
						readMeta = false;
					}
				}
				if (!readMeta) {
					if (!line.isEmpty()) {
						content.append(line);
						if (!singleLine) {
							content.append('\n');
						}
					}
				}
			}
			map.put("content", content.toString());
			return map;
		}
	}

	static final Pattern PATTERN_META = Pattern.compile("^(.+)\\=(.*)$");

}
