package com.itranswarp.shici.util;

import java.lang.reflect.Field;

public class BeanUtil {

	public static Object getField(Object bean, String fieldName) {
		Field field = lookupField(bean, fieldName);
		try {
			field.setAccessible(true);
			return field.get(bean);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	public static void setField(Object bean, String fieldName, Object value) {
		Field field = lookupField(bean, fieldName);
		try {
			field.setAccessible(true);
			field.set(bean, value);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	static Field lookupField(Object bean, String fieldName) {
		Class<?> clazz = bean.getClass();
		Field field = null;
		while (clazz != null) {
			try {
				field = clazz.getDeclaredField(fieldName);
				return field;
			} catch (NoSuchFieldException e) {
				clazz = clazz.getSuperclass();
			} catch (IllegalArgumentException e) {
				throw new RuntimeException(e);
			}
		}
		throw new RuntimeException("Field '" + fieldName + "' not found.");
	}
}
