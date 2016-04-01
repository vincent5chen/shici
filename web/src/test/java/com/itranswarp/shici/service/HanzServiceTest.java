package com.itranswarp.shici.service;

import static org.junit.Assert.*;

import org.junit.Before;

import com.itranswarp.shici.model.User;
import com.itranswarp.warpdb.Database;
import com.itranswarp.warpdb.context.UserContext;

public class HanzServiceTest extends AbstractServiceTestBase {

	HanzService hanzService;

	@Before
	public void setUp() {
		hanzService = initHanzService(database);
	}

	public HanzService initHanzService(Database db) {
		// init hanz:
		try (UserContext<User> context = new UserContext<User>(User.SYSTEM)) {
			char[][] hanzs = new char[][] { { '东', '東' }, { '台', '臺' }, { '张', '張' }, { '来', '來' }, { '后', '後' },
					{ '汉', '漢' }, { '国', '國' }, { '陈', '陳' }, { '见', '見' }, { '还', '還' }, { '详', '詳' } };
			for (char[] hanz : hanzs) {
				db.save(newHanz(hanz[0], hanz[1]));
			}
		}
		HanzService s = new HanzService();
		s.database = db;
		s.init();
		return s;
	}

	public void testToCht() {
		assertEquals("陳子昂", hanzService.toCht("陈子昂"));
	}
}
