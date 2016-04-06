package com.itranswarp.shici.search;

import com.itranswarp.warpdb.entity.BaseEntity;

public interface DocumentWrapper<T extends BaseEntity> {

	T getDocument();

}
