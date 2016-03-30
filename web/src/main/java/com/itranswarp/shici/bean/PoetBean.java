package com.itranswarp.shici.bean;

import com.itranswarp.shici.util.ValidateUtil;

public class PoetBean {

	public String dynastyId;
	public String name;
	public String description;
	public String birth;
	public String death;

	public void validate() {
		dynastyId = ValidateUtil.checkId(dynastyId);
		name = ValidateUtil.checkName(name);
		description = ValidateUtil.checkDescription(description);
		// check birth, death:
		birth = ValidateUtil.checkDateString("birth", birth);
		death = ValidateUtil.checkDateString("death", death);
	}

}
