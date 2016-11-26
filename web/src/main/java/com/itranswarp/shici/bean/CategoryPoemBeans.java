package com.itranswarp.shici.bean;

import java.util.List;

public class CategoryPoemBeans {

	public List<CategoryPoemBean> categoryPoems;

	public void validate() {
		for (CategoryPoemBean p : categoryPoems) {
			p.validate();
		}
	}
}
