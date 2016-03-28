package com.itranswarp.shici.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;

public class ListUtil {

	public static <R, T> Map<R, T> listToMap(Collection<T> list, Function<? super T, ? extends R> mapper) {
		Map<R, T> map = new HashMap<R, T>(list.size());
		for (T t : list) {
			R r = mapper.apply(t);
			map.put(r, t);
		}
		return map;
	}

	public static <R, T> Set<R> listToSet(Collection<T> list, Function<? super T, ? extends R> mapper) {
		Set<R> set = new HashSet<R>();
		for (T t : list) {
			R r = mapper.apply(t);
			set.add(r);
		}
		return set;
	}

	public static <R, T> List<T> distinct(Collection<T> list, Function<? super T, ? extends R> mapper) {
		Set<R> set = new HashSet<R>();
		List<T> rs = new ArrayList<T>(list.size());
		for (T t : list) {
			R r = mapper.apply(t);
			if (!set.contains(r)) {
				set.add(r);
				rs.add(t);
			}
		}
		return rs;
	}

	public static <R, T> List<R> map(Collection<T> list, Function<? super T, ? extends R> mapper) {
		List<R> rs = new ArrayList<R>(list.size());
		for (T t : list) {
			R r = mapper.apply(t);
			rs.add(r);
		}
		return rs;
	}

	public static <T> T reduce(List<T> list, BinaryOperator<T> accumulator) {
		Iterator<T> it = list.iterator();
		T r = it.next();
		while (it.hasNext()) {
			r = accumulator.apply(r, it.next());
		}
		return r;
	}

	public static <R, T> R reduce(List<T> list, R identity, BiFunction<R, ? super T, R> accumulator) {
		R r = identity;
		for (T t : list) {
			r = accumulator.apply(r, t);
		}
		return r;
	}

	public static <T> boolean anyMatch(Collection<T> list, Predicate<? super T> predicate) {
		for (T t : list) {
			if (predicate.test(t)) {
				return true;
			}
		}
		return false;
	}
}
