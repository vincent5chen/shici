package com.itranswarp.shici.service;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.itranswarp.search.SearchResults;
import com.itranswarp.shici.exception.APIArgumentException;
import com.itranswarp.shici.search.SearchablePoem;
import com.itranswarp.shici.search.Searcher;

@RestController
public class SearchService extends AbstractService {

	@Autowired
	Searcher searcher;

	@GetMapping("/api/search")
	public SearchResults<SearchablePoem> search(@RequestParam("q") String queryString) {
		if (queryString == null || queryString.trim().isEmpty()) {
			throw new APIArgumentException("q");
		}
		SearchResults<SearchablePoem> r = searcher.search(queryString.trim());
		return r != null ? r : EMPTY;
	}

	final SearchResults<SearchablePoem> EMPTY = new SearchResults<>(0, Collections.emptyList());
}
