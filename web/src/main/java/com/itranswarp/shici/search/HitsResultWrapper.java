package com.itranswarp.shici.search;

public interface HitsResultWrapper<T extends Searchable> {

	HitsWrapper<T> getHitsWrapper();

	Class<T> getSearchableClass();
}
