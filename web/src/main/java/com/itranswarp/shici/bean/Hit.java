package com.itranswarp.shici.bean;

/**
 * JavaBean for search hits.
 * 
 * @author liaoxuefeng
 */
public class Hit {

	public final long id;
	public final float score;

	public Hit(long id, float score) {
		this.id = id;
		this.score = score;
	}
}
