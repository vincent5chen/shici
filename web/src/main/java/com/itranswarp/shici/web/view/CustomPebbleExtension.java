package com.itranswarp.shici.web.view;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.itranswarp.shici.util.JsonUtil;
import com.mitchellbosecke.pebble.extension.AbstractExtension;
import com.mitchellbosecke.pebble.extension.Filter;

public class CustomPebbleExtension extends AbstractExtension {

	@Override
	public Map<String, Filter> getFilters() {
		Map<String, Filter> map = new HashMap<>();
		map.put("addslashes", new AddSlashesFilter());
		map.put("json", new JsonFilter());
		map.put("d", new DateFilter());
		map.put("dt", new DateTimeFilter());
		map.put("smartdt", new SmartDateTimeFilter());
		return map;
	}

}

class DateFilter implements Filter {

	final ZoneOffset OFFSET = ZoneOffset.of("+08:00");
	final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	@Override
	public List<String> getArgumentNames() {
		return null;
	}

	@Override
	public Object apply(Object input, Map<String, Object> args) {
		long n = (Long) input;
		LocalDateTime ldt = LocalDateTime.ofEpochSecond(n / 1000, 0, OFFSET);
		return ldt.format(FORMATTER);
	}
}

class DateTimeFilter implements Filter {

	final ZoneOffset OFFSET = ZoneOffset.of("+08:00");
	final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

	@Override
	public List<String> getArgumentNames() {
		return null;
	}

	@Override
	public Object apply(Object input, Map<String, Object> args) {
		long n = (Long) input;
		LocalDateTime ldt = LocalDateTime.ofEpochSecond(n / 1000, 0, OFFSET);
		return ldt.format(FORMATTER);
	}
}

class SmartDateTimeFilter implements Filter {

	final ZoneOffset OFFSET = ZoneOffset.of("+08:00");
	final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

	@Override
	public List<String> getArgumentNames() {
		return null;
	}

	@Override
	public Object apply(Object input, Map<String, Object> args) {
		long n = (Long) input;
		long current = System.currentTimeMillis();
		long minutes = (current - n) / 60000L;
		if (minutes < 1) {
			return "1分钟前";
		}
		if (minutes < 60) {
			return minutes + "分钟前";
		}
		long hours = minutes / 60L;
		if (hours < 24) {
			return hours + "小时前";
		}
		long days = hours / 24;
		if (days < 4) {
			return days + "天前";
		}
		LocalDateTime ldt = LocalDateTime.ofEpochSecond(n / 1000, 0, OFFSET);
		return ldt.format(FORMATTER);
	}
}

class AddSlashesFilter implements Filter {

	@Override
	public List<String> getArgumentNames() {
		return null;
	}

	@Override
	public Object apply(Object input, Map<String, Object> args) {
		String s = (String) input;
		return s.replace("\'", "\\\'").replace("\"", "\\\"").replace("\\", "\\\\");
	}
}

class JsonFilter implements Filter {

	@Override
	public List<String> getArgumentNames() {
		return null;
	}

	@Override
	public Object apply(Object input, Map<String, Object> args) {
		return JsonUtil.toJson(input);
	}
}
