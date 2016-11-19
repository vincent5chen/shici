package com.itranswarp.shici.app;

import java.util.Arrays;

import com.itranswarp.warpdb.WarpDb;

public class DDL {

	public static void main(String[] args) {
		WarpDb warpdb = new WarpDb();
		warpdb.setBasePackages(Arrays.asList("com.itranswarp.shici.model"));
		warpdb.init();
		System.out.println("-- Run initial schema as root --\n");
		System.out.println("CREATE DATABASE shici;\n");
		System.out.println("GRANT SELECT, INSERT, UPDATE, DELETE ON shici.* TO shici@localhost identified by \'shici\';\n");
		System.out.println(warpdb.exportSchema());
	}
}
