package com.itranswarp.shici;

import java.util.Arrays;

import org.junit.Before;

import com.itranswarp.shici.model.ForeignKeys;
import com.itranswarp.warpdb.WarpDb;

public class DatabaseTestBase {

	protected WarpDb warpdb = null;

	@Before
	public void setUpDatabase() {
		warpdb = new WarpDb();
		warpdb.setBasePackages(Arrays.asList("com.itranswarp.shici.model"));
		warpdb.setJdbcTemplate(JdbcTemplateHsqldbFactory.createJdbcTemplate());
		warpdb.init();
		for (String fk : ForeignKeys.FOREIGN_KEYS) {
			warpdb.update(fk);
		}
	}

}
