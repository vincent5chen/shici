package com.itranswarp.shici.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import com.itranswarp.shici.exception.APIArgumentException;
import com.itranswarp.shici.model.User;
import com.itranswarp.warpdb.IdUtil;

public class ValidateUtil {

	static final Pattern ALIAS_PATTERN = Pattern.compile("^[a-z0-9]{1,50}$");

	static final Pattern EMAIL_NAME_PATTERN = Pattern.compile("^[a-z0-9][a-z0-9_.-]*$");
	static final Pattern EMAIL_DOMAIN_PATTERN = Pattern.compile("^[a-z0-9][a-z0-9-]*$");

	// 1MB:
	static final int MAX_FILE_BASE64_LENGTH = 1026 * 1026 * 4 / 3;

	public static String checkId(String id) {
		return checkId(id, "id");
	}

	public static String checkId(String id, String argName) {
		if (!IdUtil.isValidId(id)) {
			throw new APIArgumentException(argName);
		}
		return id;
	}

	public static String checkIdOrEmpty(String id) {
		return checkIdOrEmpty(id, "id");
	}

	public static String checkIdOrEmpty(String id, String argName) {
		if (id == null || id.equals("")) {
			return "";
		}
		return checkId(id, argName);
	}

	public static List<String> checkIds(List<String> ids) {
		if (ids == null) {
			throw new APIArgumentException("ids", "Ids cannot be null.");
		}
		Set<String> sets = new HashSet<String>(ids.size());
		for (String id : ids) {
			if (!IdUtil.isValidId(id)) {
				throw new APIArgumentException("ids", "Ids contains invalid id.");
			}
			sets.add(id);
		}
		if (sets.size() != ids.size()) {
			throw new APIArgumentException("ids", "Contains duplicate id.");
		}
		return ids;
	}

	public static String checkNotes(String notes) {
		if (notes == null) {
			notes = "";
		}
		notes = notes.trim();
		// max to 512K:
		if (notes.length() > 524288) {
			throw new APIArgumentException("notes");
		}
		return notes;
	}

	public static String checkAlias(String alias) {
		if (alias == null) {
			throw new APIArgumentException("alias");
		}
		alias = alias.trim().toLowerCase();
		if (alias.isEmpty() || alias.length() > 50) {
			throw new APIArgumentException("alias");
		}
		if (!ALIAS_PATTERN.matcher(alias).matches()) {
			throw new APIArgumentException("alias");
		}
		return alias;
	}

	public static String checkName(String name) {
		if (name == null) {
			throw new APIArgumentException("name");
		}
		name = normalizeChinese(name);
		if (name.isEmpty() || name.length() > 100) {
			throw new APIArgumentException("name");
		}
		return name;
	}

	public static String checkDescription(String description) {
		if (description == null) {
			return "";
		}
		description = normalizeChinese(description);
		if (description.length() > 1000) {
			throw new APIArgumentException("description");
		}
		return description;
	}

	public static String checkContent(String content) {
		if (content != null) {
			content = normalizeChinese(content);
		}
		// max to 512K:
		if (content == null || content.isEmpty() || content.length() > 524288) {
			throw new APIArgumentException("content");
		}
		return content;
	}

	public static String checkAppreciation(String appreciation) {
		if (appreciation == null) {
			return "";
		}
		appreciation = normalizeChinese(appreciation);
		if (appreciation.length() > 1000) {
			throw new APIArgumentException("appreciation");
		}
		return appreciation;
	}

	public static String checkGender(String gender) {
		if (gender == null) {
			return User.Gender.UNKNOWN;
		}
		gender = gender.toLowerCase();
		if (!User.Gender.SET.contains(gender)) {
			throw new APIArgumentException("gender");
		}
		return gender;
	}

	public static long checkAdminRole(long role) {
		if (role != User.Role.ADMIN) {
			throw new APIArgumentException("role");
		}
		return role;
	}

	public static String checkEmail(String email) {
		if (email == null) {
			throw new APIArgumentException("email");
		}
		email = email.trim();
		if (email.length() > 50) {
			throw new APIArgumentException("email");
		}
		email = email.toLowerCase();
		int pos = email.indexOf('@');
		if (pos == (-1)) {
			throw new APIArgumentException("email");
		}
		if (!EMAIL_NAME_PATTERN.matcher(email.substring(0, pos)).matches()) {
			throw new APIArgumentException("email");
		}
		String[] ss = email.substring(pos + 1).split("\\.");
		if (ss.length <= 1 || ss.length >= 5) {
			throw new APIArgumentException("email");
		}
		for (String s : ss) {
			if (!EMAIL_DOMAIN_PATTERN.matcher(s).matches()) {
				throw new APIArgumentException("email");
			}
		}
		return email;
	}

	public static String checkTags(String tags) {
		if (tags == null) {
			return "";
		}
		String[] ss = tags.split("[，；\\,\\;\\s\u00a0\u3000]+");
		List<String> tagList = new ArrayList<String>(ss.length);
		for (String s : ss) {
			if (!s.isEmpty()) {
				tagList.add(s);
			}
		}
		return String.join(",", tagList);
	}

	public static String checkBase64Data(String data) {
		if (data == null || data.isEmpty()) {
			throw new APIArgumentException("data");
		}
		if (data.length() > MAX_FILE_BASE64_LENGTH) {
			throw new APIArgumentException("data", "data is too large.");
		}
		return data;
	}

	public static String checkImageData(String imageData) {
		return checkBase64Data("imageData", imageData);
	}

	public static String checkFileData(String fileData) {
		return checkBase64Data("fileData", fileData);
	}

	static String checkBase64Data(String name, String data) {
		if (data == null || data.isEmpty()) {
			throw new APIArgumentException(name);
		}
		if (data.length() > MAX_FILE_BASE64_LENGTH) {
			throw new APIArgumentException(name, "File is too large.");
		}
		return data;
	}

	public static String checkDateString(String name, String date) {
		if (date == null) {
			return "";
		}
		date = date.trim();
		if (date.length() > 10) {
			throw new APIArgumentException(name, "Date is too long.");
		}
		return date;
	}

	static final String TRIM_STRING = " " + "［］｛｝（）" + "\"\'“”[](){}0123456789\u00a0\u3000\t\r\n\0"
			+ "⑴⑵⑶⑷⑸⑹⑺⑻⑼⑽⑾⑿⒀⒁⒂⒃⒄⒅⒆⒇" + "⒈⒉⒊⒋⒌⒍⒎⒏⒐⒑⒒⒓⒔⒕⒖⒗⒘⒙⒚⒛" + "①②③④⑤⑥⑦⑧⑨⑩⑪⑫⑬⑭⑮⑯⑰⑱⑲⑳";
	static final Set<String> SHOULD_REMOVE = new HashSet<String>(Arrays.asList(TRIM_STRING.split("")));

	static final char[][] SHOULD_REPLACE = { { ',', '，' }, { '.', '。' }, { ';', '；' }, { '?', '？' }, { '!', '！' },
			{ '\u25cf', '\u00b7' }, { '\u25cb', '\u00b7' }, { '\u2299', '\u00b7' }, { '\u00b0', '\u00b7' },
			{ '\u25aa', '\u00b7' }, { '\u25ab', '\u00b7' }, { '\u2022', '\u00b7' } };

	static String normalizeChinese(String s) {
		if (s == null) {
			return "";
		}
		StringBuilder sb = new StringBuilder(s.length());
		for (int i = 0; i < s.length(); i++) {
			String ch = s.substring(i, i + 1);
			if (!SHOULD_REMOVE.contains(ch)) {
				sb.append(ch);
			}
		}
		String r = sb.toString();
		for (char[] repl : SHOULD_REPLACE) {
			r = r.replace(repl[0], repl[1]);
		}
		return r;
	}

}
