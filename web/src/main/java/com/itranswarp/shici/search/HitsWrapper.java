package com.itranswarp.shici.search;

import java.util.ArrayList;
import java.util.List;

public interface HitsWrapper<T extends Searchable> {

	List<? extends DocumentWrapper<T>> getDocumentWrappers();

	int getTotal();

	default List<T> getHits(double minScore) {
		List<? extends DocumentWrapper<T>> list = getDocumentWrappers();
		List<T> results = new ArrayList<T>(list.size());
		for (DocumentWrapper<T> t : list) {
			if (t.getScore() > minScore) {
				results.add(t.getDocument());
			}
		}
		return results;
	}
}
