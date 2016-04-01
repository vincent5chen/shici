package com.itranswarp.shici;

import java.util.Arrays;

import org.junit.Before;

import com.itranswarp.shici.model.ForeignKeys;
import com.itranswarp.warpdb.Database;

public class DatabaseTestBase {

	protected Database database = null;

	@Before
	public void setUpDatabase() {
		database = new Database();
		database.setBasePackages(Arrays.asList("com.itranswarp.shici.model"));
		database.setJdbcTemplate(JdbcTemplateHsqldbFactory.createJdbcTemplate());
		database.init();
		for (String fk : ForeignKeys.FOREIGN_KEYS) {
			database.update(fk);
		}
	}

}