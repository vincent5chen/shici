package com.itranswarp.shici.search;

import java.util.List;

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

	public static class TweetDocumentWrapper implements DocumentWrapper<Tweet> {

		public double _score;
		public Tweet _source;

		@Override
		public Tweet getDocument() {
			return _source;
		}

		@Override
		public double getScore() {
			return _score;
		}
	}

	public static class TweetHitsWrapper implements HitsWrapper<Tweet> {

		public int total;
		public List<TweetDocumentWrapper> hits;

		@Override
		public List<? extends DocumentWrapper<Tweet>> getDocumentWrappers() {
			return hits;
		}

		@Override
		public int getTotal() {
			return total;
		}
	}

	public static class TweetHitsResultWrapper implements HitsResultWrapper<Tweet> {

		public TweetHitsWrapper hits;

		@Override
		public HitsWrapper<Tweet> getHitsWrapper() {
			return hits;
		}

		@Override
		public Class<Tweet> getSearchableClass() {
			return Tweet.class;
		}

	}

}
