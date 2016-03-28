package com.itranswarp.shici.cache;

import java.util.function.Function;

public interface Cache {

	<T> T get(String key);

	<T> T get(String key, Function<String, T> fn);

	<T> void set(String key, T t);

	<T> void set(String key, T t, int seconds);

	void remove(String key);
}
