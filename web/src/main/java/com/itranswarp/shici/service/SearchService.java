package com.itranswarp.shici.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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

	@RequestMapping(value = "/api/search", method = RequestMethod.GET)
	public SearchResults<SearchablePoem> search(@RequestParam("q") String queryString) {
		if (queryString == null || queryString.trim().isEmpty()) {
			throw new APIArgumentException("q");
		}
		return searcher.search(queryString.trim());
	}

}
