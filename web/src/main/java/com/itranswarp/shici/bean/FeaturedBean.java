package com.itranswarp.shici.bean;

import java.time.LocalDate;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.itranswarp.shici.json.LocalDateDeserializer;
import com.itranswarp.shici.json.LocalDateSerializer;
import com.itranswarp.shici.util.ValidateUtil;

public class FeaturedBean {

	public String poemId;

	@JsonSerialize(using = LocalDateSerializer.class)
	@JsonDeserialize(using = LocalDateDeserializer.class)
	public LocalDate pubDate;

	public void validate() {
		poemId = ValidateUtil.checkId(poemId, "poemId");
		pubDate = ValidateUtil.checkDate(pubDate, "pubDate");
	}

}
