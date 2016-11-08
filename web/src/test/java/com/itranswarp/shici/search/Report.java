package com.itranswarp.shici.search;

import com.itranswarp.shici.model.BaseEntity;
import com.itranswarp.shici.util.IdUtils;

public class Report extends BaseEntity {
	public String username;
	public String tags;
	public String content;

	public static Report newReport(String username, String tags, String content) {
		Report r = new Report();
		r.username = username;
		r.tags = tags;
		r.content = content;
		r.id = IdUtils.next();
		r.createdBy = r.updatedBy = "00000000000000000000";
		r.createdAt = r.updatedAt = System.currentTimeMillis();
		return r;
	}
}
