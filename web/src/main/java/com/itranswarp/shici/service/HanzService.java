package com.itranswarp.shici.service;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.itranswarp.warpdb.Database;

@Component
public class HanzService {

	@Autowired
	Database database;

	@PostConstruct
	public void init() {
		List<Hanz> all = database.from(Hanz.class).list();
	}
}
