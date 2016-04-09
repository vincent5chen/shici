package com.itranswarp.shici.search;

import java.util.List;

public interface HitsWrapper<T extends Searchable> {

	List<? extends DocumentWrapper<T>> getDocumentWrappers();

	int getTotal();

}
