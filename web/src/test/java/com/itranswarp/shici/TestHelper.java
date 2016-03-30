package com.itranswarp.shici;

import java.io.IOException;
import java.util.Properties;

import org.springframework.core.io.support.PropertiesLoaderUtils;

import com.itranswarp.shici.util.EncryptUtil;

public class TestHelper {

	static Properties props = null;

	public static String getProperty(String key) {
		return getProperty(key, null);
	}

	public static String getProperty(String key, String defaultValue) {
		try {
			if (props == null) {
				props = PropertiesLoaderUtils.loadAllProperties("default.properties");
			}
			String value = props.getProperty(key, defaultValue);
			if (value != null && value.startsWith("AES:")) {
				value = EncryptUtil.decryptByAES(value.substring(4));
			}
			return value;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
