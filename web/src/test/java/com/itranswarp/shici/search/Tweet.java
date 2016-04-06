package com.itranswarp.shici.search;

import com.itranswarp.warpdb.IdUtil;
import com.itranswarp.warpdb.entity.BaseEntity;

public class Tweet extends BaseEntity {
	public String username;
	public String message;

	public static Tweet newTweet(String username, String message) {
		Tweet t = new Tweet();
		t.username = username;
		t.message = message;
		t.id = IdUtil.next();
		t.createdBy = t.updatedBy = "00000000000000000000";
		t.createdAt = t.updatedAt = System.currentTimeMillis();
		return t;
	}
}
