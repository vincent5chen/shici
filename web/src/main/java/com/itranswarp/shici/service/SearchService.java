package com.itranswarp.shici.service;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.itranswarp.shici.exception.APIArgumentException;
import com.itranswarp.shici.model.Poem;
import com.itranswarp.shici.search.SearchResultException;
import com.itranswarp.shici.search.Searcher;
import com.itranswarp.shici.searchable.SearchablePoem;
import com.itranswarp.shici.util.ValidateUtil;

@RestController
public class SearchService extends AbstractService {

	static final String INDEX_NAME = "shici";

	@Autowired
	HanziService hanziService;

	@Autowired
	Searcher searcher;

	@RequestMapping(value = "/api/search", method = RequestMethod.GET)
	public List<SearchablePoem> search(@RequestParam("q") String queryString) {
		if (queryString == null) {
			throw new APIArgumentException("q");
		}
		queryString = queryString.trim();
		if (queryString.isEmpty()) {
			throw new APIArgumentException("q");
		}
		String[] ss = hanziService.toChs(queryString).split("\\s+");
		List<String> qs = new ArrayList<String>();
		for (String s : ss) {
			String q = ValidateUtil.normalizeChinese(s);
			if (!q.isEmpty() && qs.size() < 3) {
				qs.add(q.length() <= 7 ? q : q.substring(0, 7));
			}
		}
		if (qs.isEmpty()) {
			throw new SearchResultException();
		}
		return searcher.search(INDEX_NAME, SearchablePoem.class, qs.toArray(new String[qs.size()]), 20);
	}

	@PostConstruct
	public void init() {
		// check index exists?
		if (!searcher.indexExist(INDEX_NAME)) {
			log.info("Index not found. Create new index...");
			searcher.createIndex(INDEX_NAME);
			log.info("Start full index...");
			new Thread() {
				@Override
				public void run() {
					indexAllPoems();
				}
			}.start();
		}
	}

	void indexAllPoems() {
		long updatedAt = 0;
		int maxResults = 200;
		int count = 0;
		for (;;) {
			List<Poem> poems = warpdb.list(Poem.class,
					"select * from Poem where updatedAt>? order by updatedAt limit ?", updatedAt, maxResults);
			if (poems.isEmpty()) {
				break;
			}
			updatedAt = indexPoems(poems);
			count += poems.size();
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
			searcher.createDocument(INDEX_NAME, new SearchablePoem(poem));
			lastUpdatedAt = poem.updatedAt;
		}
		log.info(poems.size() + " poems indexed.");
		return lastUpdatedAt;
	}

}
