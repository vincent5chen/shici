package com.itranswarp.shici.search;

import com.itranswarp.warpdb.IdUtil;

public class Tweet implements Searchable {

	public String id;

	public long score;

	@Analyzed
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
		return t;
	}

	@Override
	public String getId() {
		return this.id;
	}

}
