package com.itranswarp.shici.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import com.itranswarp.shici.model.Hanz;

@Component
public class HanzService extends AbstractService {

	Map<Character, Character> chtMap;

	@PostConstruct
	public void init() {
		List<Hanz> all = database.from(Hanz.class).list();
		Map<Character, Character> map = new HashMap<Character, Character>();
		for (Hanz hanz : all) {
			if (hanz.s.isEmpty() || hanz.t.isEmpty()) {
				log.warn("s or t is empty: " + hanz);
			}
			map.put(hanz.s.charAt(0), hanz.t.charAt(0));
		}
		log.info(all.size() + " characters loaded.");
		chtMap = map;
	}

	public String toCht(String chs) {
		StringBuilder sb = new StringBuilder(chs.length());
		for (int i = 0; i < chs.length(); i++) {
			char ch = chs.charAt(i);
			Character t = chtMap.get(ch);
			sb.append(t == null ? ch : t);
		}
		return sb.toString();
	}
}
