package com.itranswarp.shici.bean;

import com.itranswarp.shici.util.ValidateUtil;

public class UserBean {

	public String email;
	public String name;
	public String gender;

	public void validate() {
		email = ValidateUtil.checkEmail(email);
		gender = ValidateUtil.checkGender(gender);
		name = ValidateUtil.checkName(name);
	}

}
