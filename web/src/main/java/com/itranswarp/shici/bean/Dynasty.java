package com.itranswarp.shici.bean;

/**
 * JavaBean for dynasty.
 * 
 * @author liaoxuefeng
 */
public class Dynasty implements Comparable<Dynasty> {

	private long id;
	private String name;

	public Dynasty(long id, String name) {
		this.id = id;
		this.name = name;
	}

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return String.format("{Dynasty: id=%016x, name=%s}", this.id, this.name);
	}

	@Override
	public int compareTo(Dynasty o) {
		return Long.compare(this.id, o.id);
	}
}
