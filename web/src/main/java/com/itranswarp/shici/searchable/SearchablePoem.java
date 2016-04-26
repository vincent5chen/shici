package com.itranswarp.shici.searchable;

import com.itranswarp.shici.model.Poem;
import com.itranswarp.shici.search.Analyzed;
import com.itranswarp.shici.search.Searchable;

public class SearchablePoem implements Searchable {

	public long form;
	public long rating;

	public String poemId;
	public String poetId;
	public String dynastyId;

	@Analyzed
	public String name;
	public String nameCht;

	@Analyzed
	public String poetName;
	public String poetNameCht;

	@Analyzed
	public String content;
	public String contentCht;

	public SearchablePoem() {
	}

	public SearchablePoem(Poem poem) {
		this.form = poem.form;
		this.rating = poem.imageId.isEmpty() ? 0 : 1;

		this.poemId = poem.id;
		this.poetId = poem.poetId;
		this.dynastyId = poem.dynastyId;

		this.name = poem.name;
		this.nameCht = poem.nameCht;
		this.poetName = poem.poetName;
		this.poetNameCht = poem.poetNameCht;
		this.content = poem.content;
		this.contentCht = poem.contentCht;
	}

	@Override
	public String getId() {
		return this.poemId;
	}
}
