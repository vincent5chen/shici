package com.itranswarp.shici.bean;

import com.itranswarp.shici.util.ValidateUtil;

public class StaticPageBean {

	public String alias;
	public String name;
	public String content;

	public void validate() {
		alias = ValidateUtil.checkAlias(alias);
		name = ValidateUtil.checkName(name);
		content = ValidateUtil.checkContent(content);
	}
}
