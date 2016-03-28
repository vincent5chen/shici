package com.itranswarp.shici.ddl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.util.Arrays;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.dialect.MySQL5InnoDBDialect;

import com.itranswarp.shici.util.EncryptUtil;
import com.itranswarp.shici.util.FileUtil;
import com.itranswarp.warpdb.DDLGenerator;

public class DDL {

	static final Log log = LogFactory.getLog(DDL.class);

	public static void main(String[] args) throws Exception {
		File file = new File(".").getAbsoluteFile();
		String schemaOutput = file.getCanonicalPath() + File.separator + "target" + File.separator + "ddl.sql";
		String initOutput = file.getCanonicalPath() + File.separator + "target" + File.separator + "init.sql";
		DDLGenerator generator = new DDLGenerator();
		generator.export(Arrays.asList("com.itranswarp.shici"), MySQL5InnoDBDialect.class, schemaOutput);
		generateInitOutput(initOutput);
		log.info("Database initialize script was successfully exported to file: " + initOutput);
		log.info("DDL script was successfully exported to file: " + schemaOutput);
		System.out.println("");
		System.out.println("------------------------------------------------------------");
		System.out.println("  WARNING:");
		System.out.println("  Copy and run the following command to init database.");
		System.out.println("------------------------------------------------------------");
		System.out.println("");
		String allOutput = file.getCanonicalPath() + File.separator + "target" + File.separator + "all.sql";
		FileUtil.writeString(allOutput, FileUtil.readAsString(initOutput) + "\n" + FileUtil.readAsString(schemaOutput)
				+ "\n\nselect \'database init ok.\' as \'MESSAGE:\';");
		System.out.println(String.join(" ", "mysql", "-uroot", "-p", "<", allOutput));
	}

	static void generateInitOutput(String file) throws Exception {
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
		try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"))) {
			writer.write("drop database if exists " + database + END);
			writer.write("create database " + database + END);
			writer.write("grant all on " + database + ".* to \'" + user + "\'@\'localhost\' identified by \'" + password
					+ "\'" + END);
			writer.write("use " + database + END);
		}
	}

}
