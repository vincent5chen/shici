package com.itranswarp.shici.bean;

import java.util.List;

import com.itranswarp.shici.util.ValidateUtil;

public class IdsBean {

	public List<String> ids;

	public void validate() {
		ValidateUtil.checkIds(ids);
	}
}
