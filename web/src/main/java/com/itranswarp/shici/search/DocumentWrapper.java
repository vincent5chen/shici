package com.itranswarp.shici.search;

/**
 * Internal use to generate document wrapper class.
 * 
 * @author michael
 *
 * @param <T> Generic type.
 */
public interface DocumentWrapper<T extends Searchable> {

	T getDocument();

	double getScore();

}
