package com.itranswarp.shici.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;

/**
 * Utils class.
 * 
 * @author liaoxuefeng
 */
public class Utils {

	public static int hashAsInt(String data) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			md.update(data.getBytes(StandardCharsets.UTF_8));
			byte[] hash = md.digest();
			return ((0xff & hash[0]) << 24) | ((0xff & hash[1]) << 16) | ((0xff & hash[2]) << 8) | (0xff & hash[3]);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	public static Resource[] loadResources(String path) throws IOException {
		ResourcePatternResolver resourcePatternResolver = ResourcePatternUtils.getResourcePatternResolver(null);
		return resourcePatternResolver.getResources(path);
	}

	public static <K, V> Map<K, V> ofMap(K k1, V v1) {
		Map<K, V> map = new HashMap<>();
		map.put(k1, v1);
		return map;
	}

	public static <K, V> Map<K, V> ofMap(K k1, V v1, K k2, V v2) {
		Map<K, V> map = new HashMap<>();
		map.put(k1, v1);
		map.put(k2, v2);
		return map;
	}

	public static <K, V> Map<K, V> ofMap(K k1, V v1, K k2, V v2, K k3, V v3) {
		Map<K, V> map = new HashMap<>();
		map.put(k1, v1);
		map.put(k2, v2);
		map.put(k3, v3);
		return map;
	}

}
