package com.itranswarp.shici;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.dialect.MySQL5InnoDBDialect;

import com.itranswarp.shici.model.ForeignKeys;
import com.itranswarp.shici.util.EncryptUtil;
import com.itranswarp.shici.util.FileUtil;
import com.itranswarp.warpdb.DDLGenerator;

public class DDL {

	static final Log log = LogFactory.getLog(DDL.class);

	public static void main(String[] args) throws Exception {
		File file = new File(".").getAbsoluteFile();
		String schemaOutput = file.getCanonicalPath() + File.separator + "target" + File.separator + "ddl.sql";
		DDLGenerator generator = new DDLGenerator();
		generator.export(Arrays.asList("com.itranswarp.shici"), MySQL5InnoDBDialect.class, schemaOutput);
		String preInitOutput = generatePreInitOutput();
		String postInitOutput = generatePostInitOutput();
		log.info("Database initialize script was successfully exported to file: " + preInitOutput);
		log.info("DDL script was successfully exported to file: " + schemaOutput);
		String allOutput = file.getCanonicalPath() + File.separator + "target" + File.separator + "all.sql";
		FileUtil.writeString(allOutput, preInitOutput + "\n" + FileUtil.readAsString(schemaOutput) + "\n"
				+ postInitOutput + "\n\nselect \'database init ok.\' as \'MESSAGE:\';");

		System.out.println("");
		System.out.println("------------------------------------------------------------");
		System.out.println("  WARNING:");
		System.out.println("  Copy and run the following command to init database.");
		System.out.println("------------------------------------------------------------");
		System.out.println("");
		System.out.println(String.join(" ", "mysql", "-uroot", "-p", "<", allOutput));
	}

	static String generatePreInitOutput() throws Exception {
		String propertyName = "default.properties";
		URL resource = DDL.class.getClassLoader().getResource(propertyName);
		if (resource == null) {
			throw new IOException("Properties file not found: " + propertyName);
		}
		Properties props = new Properties();
		props.load(resource.openStream());
		String url = props.getProperty("jdbc.url");
		String user = props.getProperty("jdbc.user");
		String password = props.getProperty("jdbc.password");
		if (password.startsWith("AES:")) {
			password = EncryptUtil.decryptByAES(password.substring(4));
		}
		String database = url.substring(url.lastIndexOf("/") + 1);
		final String END = ";\n\n";
		List<String> list = new ArrayList<String>();
		list.add("drop database if exists " + database + END);
		list.add("create database " + database + END);
		list.add("grant all on " + database + ".* to \'" + user + "\'@\'localhost\' identified by \'" + password + "\'"
				+ END);
		list.add("use " + database + END);
		return String.join("", list);
	}

	static String generatePostInitOutput() throws Exception {
		return String.join("\n\n", ForeignKeys.FOREIGN_KEYS);
	}
}
