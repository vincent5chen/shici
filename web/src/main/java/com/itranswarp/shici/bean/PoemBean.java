package com.itranswarp.shici.bean;

import java.io.IOException;

import com.itranswarp.shici.exception.APIArgumentException;
import com.itranswarp.shici.exception.APIException;
import com.itranswarp.shici.model.Poem;
import com.itranswarp.shici.util.ValidateUtil;

public class PoemBean {

	public String poetId;
	public String name;
	public String content;
	public String appreciation;
	public long form;
	public String tags;
	public String imageData;

	public void validate() {
		poetId = ValidateUtil.checkId(poetId);
		name = ValidateUtil.checkName(name);
		content = ValidateUtil.checkContent(content);
		appreciation = ValidateUtil.checkAppreciation(appreciation);
		// check form:
		boolean isValidForm = false;
		for (long f : Poem.Form.ALL) {
			if (f == form) {
				isValidForm = true;
				break;
			}
		}
		if (!isValidForm) {
			throw new APIArgumentException("form");
		}
		tags = ValidateUtil.checkTags(tags);
		try {
			imageData = ValidateUtil.checkImageData(imageData);
		} catch (IOException e) {
			throw new APIException("IOException", e);
		}
	}

}
