package com.itranswarp.shici.bean;

import com.itranswarp.shici.util.ValidateUtil;

public class UserProfileBean {

	public String email;
	public String gender;

	public void validate() {
		if (email != null && !email.isEmpty()) {
			email = ValidateUtil.checkEmail(email);
		}
		gender = ValidateUtil.checkGender(gender);
	}

}
