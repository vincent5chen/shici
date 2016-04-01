package com.itranswarp.shici;

import java.util.Arrays;

import org.junit.Before;

import com.itranswarp.warpdb.Database;

public class DatabaseTestBase {

	protected Database database = null;

	@Before
	public void setUpDatabase() {
		database = new Database();
		database.setBasePackages(Arrays.asList("com.itranswarp.shici.model"));
		database.setJdbcTemplate(JdbcTemplateHsqldbFactory.createJdbcTemplate());
		database.init();
		database.update("alter table Poem add foreign key(poetId) references Poem(id)");
	}

}