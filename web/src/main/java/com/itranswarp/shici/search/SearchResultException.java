package com.itranswarp.shici.search;

import java.util.Map;

import com.itranswarp.wxapi.util.JsonUtil;

public class SearchResultException extends SearchException {

	public Map<String, Object> error;
	public int status;

	@Override
	public String toString() {
		return "SearchResultException:\nstatus=" + status + "\nerror: " + JsonUtil.toJson(error);
	}
}
