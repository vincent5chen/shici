package com.itranswarp.shici.bean;

/**
 * JavaBean for poet.
 * 
 * @author liaoxuefeng
 */
public class Poet implements Comparable<Poet> {

	Dynasty dynasty;

	long id;
	String name;
	String profile;
	String birth;
	String death;

	public Poet(Dynasty dynasty, long id, String name, String profile, String birth, String death) {
		this.dynasty = dynasty;
		this.id = id;
		this.name = name;
		this.profile = profile;
		this.birth = birth;
		this.death = death;
	}

	public long getId() {
		return this.id;
	}

	public Dynasty getDynasty() {
		return this.dynasty;
	}

	public String getName() {
		return this.name;
	}

	public String getProfile() {
		return this.profile;
	}

	@Override
	public String toString() {
		return String.format("{Poet: id=%016x, dynasty=%s, name=%s, profile=%s, birth=%s, death=%s}", this.id,
				this.dynasty.getName(), this.name, this.profile, this.birth, this.death);
	}

	@Override
	public int compareTo(Poet p) {
		int n = this.name.compareTo(p.name);
		return n != 0 ? n : Long.compare(this.id, p.id);
	}
}
