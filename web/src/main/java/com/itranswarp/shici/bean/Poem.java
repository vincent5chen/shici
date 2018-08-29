package com.itranswarp.shici.bean;

/**
 * JavaBean for poem.
 * 
 * @author liaoxuefeng
 */
public class Poem implements Comparable<Poem> {

	long id;
	Poet poet;
	String form;
	String[] tags;
	String name;
	String content;

	public Poem(Poet poet, long id, String form, String[] tags, String name, String content) {
		this.id = id;
		this.poet = poet;
		this.form = form;
		this.tags = tags;
		this.name = name;
		this.content = content;
	}

	public long getId() {
		return this.id;
	}

	public Poet getPoet() {
		return this.poet;
	}

	public String getForm() {
		return this.form;
	}

	public String[] getTags() {
		return this.tags;
	}

	public String getName() {
		return this.name;
	}

	public String getContent() {
		return content;
	}

	@Override
	public String toString() {
		return String.format("{Poem: id=%016x, poet=%s, form=%s, tags=%s, name=%s, content=%s...}", this.id,
				this.poet.getId(), this.form, String.join(",", this.tags), this.name,
				this.content.substring(0, this.content.indexOf('\n')));
	}

	@Override
	public int compareTo(Poem p) {
		int n = this.name.compareTo(p.name);
		return n != 0 ? n : Long.compare(this.id, p.id);
	}

}
