package com.itranswarp.shici.search;

import java.util.List;

public interface HitsResultWrapper<T extends Searchable> {

	HitsWrapper<T> getHitsWrapper();

	Class<T> getSearchableClass();

	default List<T> getHits(double minScore) {
		return getHitsWrapper().getHits(minScore);
	}
}
