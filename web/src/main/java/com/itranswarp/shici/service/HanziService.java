package com.itranswarp.shici.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import com.itranswarp.shici.model.Hanzi;

@Component
public class HanziService extends AbstractService {

	Map<Character, Character> chtMap;
	Map<Character, Character> chsMap;

	@PostConstruct
	public void init() {
		List<Hanzi> all = warpdb.from(Hanzi.class).list();
		Map<Character, Character> theChtMap = new HashMap<Character, Character>();
		Map<Character, Character> theChsMap = new HashMap<Character, Character>();
		for (Hanzi hanz : all) {
			if (hanz.s.isEmpty() || hanz.t.isEmpty()) {
				log.warn("s or t is empty: " + hanz);
			}
			Character s = hanz.s.charAt(0);
			Character t = hanz.t.charAt(0);
			theChtMap.put(s, t);
			theChsMap.put(t, s);
		}
		log.info(all.size() + " characters loaded.");
		chtMap = theChtMap;
		chsMap = theChsMap;
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

	public String toChs(String cht) {
		StringBuilder sb = new StringBuilder(cht.length());
		for (int i = 0; i < cht.length(); i++) {
			char ch = cht.charAt(i);
			Character t = chsMap.get(ch);
			sb.append(t == null ? ch : t);
		}
		return sb.toString();
	}
}
