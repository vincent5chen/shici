package com.itranswarp.shici.service;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.itranswarp.shici.model.Poem;
import com.itranswarp.shici.search.Analyzed;
import com.itranswarp.shici.search.DocumentWrapper;
import com.itranswarp.shici.search.HitsResultWrapper;
import com.itranswarp.shici.search.HitsWrapper;
import com.itranswarp.shici.search.SearchResultException;
import com.itranswarp.shici.search.Searchable;
import com.itranswarp.shici.search.Searcher;
import com.itranswarp.shici.util.ValidateUtil;

@Component
public class SearchService extends AbstractService {

	static final String INDEX_NAME = "shici";

	@Autowired
	HanziService hanziService;

	@Autowired
	Searcher searcher;

	public List<SearchablePoem> search(String queryString) {
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
		return searcher.search(INDEX_NAME, SearchablePoemHitsResultWrapper.class, qs.toArray(new String[qs.size()]),
				20);
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
			List<Poem> poems = database.list(Poem.class,
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

	public static class SearchablePoem implements Searchable {

		public long form;
		public long rating;

		public String poemId;
		public String poetId;
		public String dynastyId;

		@Analyzed
		public String name;
		public String nameCht;

		@Analyzed
		public String poetName;
		public String poetNameCht;

		@Analyzed
		public String content;
		public String contentCht;

		public SearchablePoem() {
		}

		public SearchablePoem(Poem poem) {
			this.form = poem.form;
			this.rating = poem.imageId.isEmpty() ? 0 : 1;

			this.poemId = poem.id;
			this.poetId = poem.poetId;
			this.dynastyId = poem.dynastyId;

			this.name = poem.name;
			this.nameCht = poem.nameCht;
			this.poetName = poem.poetName;
			this.poetNameCht = poem.poetNameCht;
			this.content = poem.content;
			this.contentCht = poem.contentCht;
		}

		@Override
		public String getId() {
			return this.poemId;
		}
	}

	public static class SearchablePoemDocumentWrapper implements DocumentWrapper<SearchablePoem> {

		public double _score;
		public SearchablePoem _source;

		@Override
		public SearchablePoem getDocument() {
			return _source;
		}

		@Override
		public double getScore() {
			return _score;
		}
	}

	public static class SearchablePoemHitsWrapper implements HitsWrapper<SearchablePoem> {

		public int total;
		public List<SearchablePoemDocumentWrapper> hits;

		@Override
		public List<? extends DocumentWrapper<SearchablePoem>> getDocumentWrappers() {
			return hits;
		}

		@Override
		public int getTotal() {
			return total;
		}
	}

	public static class SearchablePoemHitsResultWrapper implements HitsResultWrapper<SearchablePoem> {

		public SearchablePoemHitsWrapper hits;

		@Override
		public HitsWrapper<SearchablePoem> getHitsWrapper() {
			return hits;
		}

		@Override
		public Class<SearchablePoem> getSearchableClass() {
			return SearchablePoem.class;
		}

	}
}
