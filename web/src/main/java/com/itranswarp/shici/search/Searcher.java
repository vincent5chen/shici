package com.itranswarp.shici.search;

import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.itranswarp.search.SearchResults;
import com.itranswarp.search.SearchableClient;
import com.itranswarp.shici.model.Poem;
import com.itranswarp.warpdb.WarpDb;

@Component
public class Searcher {

	static final String INDEX_NAME = "shici";

	static final SearchResults<SearchablePoem> EMPTY = new SearchResults<>(0, Collections.emptyList());

	final Log log = LogFactory.getLog(getClass());

	@Autowired
	WarpDb warpdb;

	@Value("${search.host}")
	String host;

	@Value("${search.port:9300}")
	int port;

	SearchableClient client;

	@PostConstruct
	public void init() throws Exception {
		SearchableClient client = new SearchableClient();
		client.setBasePackage(SearchablePoem.class.getPackage().getName());
		client.setHost(host);
		client.setIndex(INDEX_NAME);
		client.setMaxResults(20);
		client.setPort(port);
		try {
			client.init();
			if (client.createIndex()) {
				rebuildIndex();
			}
		} catch (Exception e) {
			log.warn("Cannot connect to elastic search. Search is disabled.", e);
			client.close();
			return;
		}
		this.client = client;
	}

	public SearchResults<SearchablePoem> search(String words) {
		if (client == null) {
			return EMPTY;
		}
		return client.search(SearchablePoem.class, words);
	}

	public void index(SearchablePoem poem) {
		if (client != null) {
			client.index(poem);
		}
	}

	public void unindex(String poemId) {
		if (client != null) {
			client.unindex(SearchablePoem.class, poemId);
		}
	}

	void rebuildIndex() {
		log.info("Index not exist. Create and rebuild full index...");
		new Thread() {
			@Override
			public void run() {
				indexAllPoems();
			}
		}.start();
	}

	void indexAllPoems() {
		long updatedAt = 0;
		int maxResults = 200;
		int count = 0;
		for (;;) {
			List<Poem> poems = warpdb.from(Poem.class).where("updatedAt > ?", updatedAt).orderBy("updatedAt")
					.limit(maxResults).list();
			if (poems.isEmpty()) {
				break;
			}
			updatedAt = indexPoems(poems);
			count += poems.size();
			log.info(poems.size() + " poems indexed.");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		}
		log.info("Full index done: " + count + " documents indexed.");
	}

	long indexPoems(List<Poem> poems) {
		long lastUpdatedAt = 0;
		for (Poem poem : poems) {
			index(new SearchablePoem(poem));
			lastUpdatedAt = poem.updatedAt;
		}
		return lastUpdatedAt;
	}

}
