package com.itranswarp.shici.service;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.itranswarp.shici.model.User;
import com.itranswarp.warpdb.Database;
import com.itranswarp.warpdb.context.UserContext;

public class HanzServiceTest extends AbstractServiceTestBase {

	HanziService hanziService;

	@Before
	public void setUp() {
		hanziService = initHanziService(database);
	}

	public HanziService initHanziService(Database db) {
		// init hanz:
		try (UserContext<User> context = new UserContext<User>(User.SYSTEM)) {
			char[][] hanzs = new char[][] { { '东', '東' }, { '台', '臺' }, { '张', '張' }, { '来', '來' }, { '后', '後' },
					{ '汉', '漢' }, { '国', '國' }, { '陈', '陳' }, { '见', '見' }, { '还', '還' }, { '详', '詳' }, { '诗', '詩'} };
			for (char[] hanz : hanzs) {
				db.save(newHanz(hanz[0], hanz[1]));
			}
		}
		HanziService s = new HanziService();
		s.database = db;
		s.init();
		return s;
	}

	@Test
	public void testToCht() {
		assertEquals("陳子昂 東漢", hanziService.toCht("陈子昂 东汉"));
	}

	@Test
	public void testToChs() {
		assertEquals("陈子昂 东汉", hanziService.toChs("陳子昂 東漢"));
	}
}
