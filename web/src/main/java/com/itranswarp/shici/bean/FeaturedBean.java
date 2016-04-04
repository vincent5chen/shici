package com.itranswarp.shici.bean;

import java.time.LocalDate;

import com.itranswarp.shici.util.ValidateUtil;

public class FeaturedBean {

	public String poemId;
	public LocalDate pubDate;

	public void validate() {
		poemId = ValidateUtil.checkId(poemId, "poemId");
		pubDate = ValidateUtil.checkDate(pubDate, "pubDate");
	}

}
