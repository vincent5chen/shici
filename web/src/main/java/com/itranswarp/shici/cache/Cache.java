package com.itranswarp.shici.cache;

public interface Cache {

	<T> T get(String key);

	<T> void set(String key, T t);

	<T> void set(String key, T t, int seconds);

	void remove(String key);
}
