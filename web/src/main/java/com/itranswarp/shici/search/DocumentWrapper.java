package com.itranswarp.shici.search;

public interface DocumentWrapper<T extends Searchable> {

	T getDocument();

	double getScore();

}
