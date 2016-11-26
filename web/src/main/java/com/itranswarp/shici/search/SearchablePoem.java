package com.itranswarp.shici.search;

import com.itranswarp.search.SearchableDocument;
import com.itranswarp.search.SearchableField;
import com.itranswarp.search.SearchableId;
import com.itranswarp.shici.model.Poem;

@SearchableDocument
public class SearchablePoem {

	@SearchableId
	public String id;

	@SearchableField
	public String poetId;
	@SearchableField
	public String dynastyId;
	@SearchableField
	public String imageId;

	@SearchableField
	public long form;

	@SearchableField(boost = 5)
	public String name;
	@SearchableField(boost = 5)
	public String nameCht;

	@SearchableField(boost = 3)
	public String poetName;
	@SearchableField(boost = 3)
	public String poetNameCht;

	@SearchableField
	public String content;
	@SearchableField
	public String contentCht;

	public SearchablePoem() {
	}

	public SearchablePoem(Poem poem) {
		this.form = poem.form;

		this.id = poem.id;
		this.poetId = poem.poetId;
		this.dynastyId = poem.dynastyId;
		this.imageId = poem.imageId;

		this.name = poem.name;
		this.nameCht = poem.nameCht;
		this.poetName = poem.poetName;
		this.poetNameCht = poem.poetNameCht;
		this.content = poem.content;
		this.contentCht = poem.contentCht;
	}

}
