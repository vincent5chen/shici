package com.itranswarp.shici.bean;

import com.itranswarp.shici.util.ValidateUtil;

public class CategoryBean {

	public String name;
	public String description;

	public void validate() {
		name = ValidateUtil.checkName(name);
		description = ValidateUtil.checkDescription(description);
	}

}
