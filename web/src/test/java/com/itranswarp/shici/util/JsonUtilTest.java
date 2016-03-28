package com.itranswarp.shici.util;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.junit.Test;

public class JsonUtilTest {

	@Test
	public void testParseAsMap() {
		Map<String, String> map = JsonUtil.parseAsMap("{ \"name\": \"Michael\", \"code\": \"A-001\" }");
		assertEquals("Michael", map.get("name"));
		assertEquals("A-001", map.get("code"));
	}

	@Test
	public void testParseAsMapReturnsNull() {
		assertNull(JsonUtil.parseAsMap("null"));
	}

	@Test
	public void testToJsonWithNull() {
		assertEquals("null", JsonUtil.toJson(null));
	}

	@Test
	public void testFromJsonReturnsNull() {
		assertNull(JsonUtil.fromJson(String.class, "null"));
		assertNull(JsonUtil.fromJson(List.class, "null"));
		assertNull(JsonUtil.fromJson(Map.class, "null"));
		assertNull(JsonUtil.fromJson(TestBean.class, "null"));
	}

	@Test
	public void testToJsonAndFromJson() {
		TestBean bean = new TestBean("Test", true, 10, new TestSubBean("A-1", 10.5f), new TestSubBean("A-2", 20.25f));
		String s = JsonUtil.toJson(bean);
		assertTrue(s.contains("\"children\":["));
		TestBean parsed = JsonUtil.fromJson(TestBean.class, s);
		assertEquals(bean, parsed);
	}
}

class TestBean {
	public String name;
	public boolean gender;
	public Integer age;
	public TestSubBean[] children;

	public TestBean() {
	}

	public TestBean(String name, boolean gender, int age, TestSubBean... children) {
		super();
		this.name = name;
		this.gender = gender;
		this.age = age;
		this.children = children;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o instanceof TestBean) {
			TestBean t = (TestBean) o;
			return Objects.equals(this.name, t.name) && Objects.equals(this.gender, t.gender)
					&& Objects.equals(this.age, t.age) && Objects.deepEquals(this.children, t.children);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.name, this.gender, this.age, this.children);
	}
}

class TestSubBean {
	public String id;
	public float price;

	public TestSubBean() {
	}

	public TestSubBean(String id, float price) {
		super();
		this.id = id;
		this.price = price;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o instanceof TestSubBean) {
			TestSubBean t = (TestSubBean) o;
			return Objects.equals(this.id, t.id) && Objects.equals(this.price, t.price);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.id, this.price);
	}
}
