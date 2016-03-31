package com.itranswarp.shici;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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
				Properties p = PropertiesLoaderUtils.loadAllProperties("default.properties");
				loadExternalProperties(p);
				props = p;
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

	static void loadExternalProperties(Properties p) {
		File f = new File("/srv/shici/config.properties");
		if (f.isFile()) {
			try (InputStream input = new FileInputStream(f)) {
				p.load(input);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
