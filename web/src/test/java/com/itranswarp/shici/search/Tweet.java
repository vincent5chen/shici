package com.itranswarp.shici.search;

import com.itranswarp.warpdb.IdUtil;
import com.itranswarp.warpdb.entity.BaseEntity;

public class Tweet extends BaseEntity {

	public long score;

	public String username;

	@Analyzed
	public String title;

	@Analyzed
	public String message;

	public static Tweet newTweet(String username, String title, String message) {
		Tweet t = new Tweet();
		t.username = username;
		t.title = title;
		t.message = message;
		t.score = (long) (Math.random() * 100000000);
		t.id = IdUtil.next();
		t.createdBy = t.updatedBy = "00000000000000000000";
		t.createdAt = t.updatedAt = System.currentTimeMillis();
		return t;
	}
}
