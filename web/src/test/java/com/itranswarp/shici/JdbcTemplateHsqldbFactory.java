package com.itranswarp.shici;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Arrays;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import com.itranswarp.warpdb.WarpDb;

/**
 * Create a in-memory hsqldb and return JdbcTemplate.
 * 
 * @author michael
 */
public class JdbcTemplateHsqldbFactory {

	static final Log log = LogFactory.getLog(JdbcTemplateHsqldbFactory.class);

	public static JdbcTemplate createJdbcTemplate() {
		try {
			DataSource dataSource = new DriverManagerDataSource("jdbc:hsqldb:mem:testdb" + nextDbId(), "SA", "");
			// init database:
			String[] sqls = generateDDL().split(";");
			Connection conn = dataSource.getConnection();
			Statement stmt = conn.createStatement();
			for (String sql : sqls) {
				if (sql != null && !sql.trim().isEmpty()) {
					String s = toHsqldb(sql);
					log.info("Execute SQL: " + s);
					stmt.executeUpdate(s);
				}
			}
			stmt.close();
			conn.close();
			return new JdbcTemplate(dataSource);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	static String toHsqldb(String sql) {
		// hsqldb do not support text, mediumtext:
		sql = sql.trim().replace("MEDIUMTEXT", "longvarchar").replace("TEXT", "longvarchar");
		sql = String.join("\n", Arrays.stream(sql.split("\n")).filter((line) -> {
			return !line.trim().startsWith("INDEX ");
		}).toArray(String[]::new));
		return sql;
	}

	static int nextDbId() {
		return next++;
	}

	static int next = 0;

	static String ddl = null;

	static String generateDDL() throws Exception {
		if (ddl == null) {
			WarpDb warpdb = new WarpDb();
			warpdb.setBasePackages(Arrays.asList("com.itranswarp.shici.model"));
			warpdb.init();
			ddl = warpdb.exportSchema();
		}
		return ddl;
	}
}
