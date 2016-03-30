package com.itranswarp.shici.util;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class ValidateUtilTest {

	@Parameters
	public static Collection<String[]> data() {
		return Arrays.asList(new String[][] { { null, "" }, { "  \u00a0\u3000", "" }, { "  \"\'“”[](0123){} ", "" },
				{ "  鹅,鹅, 鹅,  \n\u3000 曲项向天歌. ", "鹅，鹅，鹅，曲项向天歌。" }, { " 唐 ▪ 李白", "唐·李白" }, { " \'侠客\"行① \r", "侠客行" },
				{ " 望[1]庐山瀑布 ⑸ ", "望庐山瀑布" }, { "［杜甫］", "杜甫" } });
	}

	String input;
	String expected;

	public ValidateUtilTest(String input, String expected) {
		this.input = input;
		this.expected = expected;
	}

	@Test
	public void testNormalizeChinese() {
		assertEquals(expected, ValidateUtil.normalizeChinese(input));
	}

}
