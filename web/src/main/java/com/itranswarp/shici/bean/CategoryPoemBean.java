package com.itranswarp.shici.bean;

import java.util.List;

import com.itranswarp.shici.util.ValidateUtil;

public class CategoryPoemBean {

	public String sectionName;
	public List<String> ids;

	public void validate() {
		sectionName = ValidateUtil.checkName(sectionName);
		ValidateUtil.checkIds(ids);
	}
}
