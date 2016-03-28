package com.itranswarp.shici.util;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import com.itranswarp.shici.exception.APIArgumentException;
import com.itranswarp.shici.model.User;
import com.itranswarp.warpdb.IdUtil;

public class ValidateUtil {

	static final Pattern ALIAS_PATTERN = Pattern.compile("^[a-z0-9]{1,50}$");

	static final Pattern CODE_PATTERN = Pattern.compile("^[a-z0-9]{4,10}$");

	static final Pattern EMAIL_NAME_PATTERN = Pattern.compile("^[a-z0-9][a-z0-9_.-]*$");
	static final Pattern EMAIL_DOMAIN_PATTERN = Pattern.compile("^[a-z0-9][a-z0-9-]*$");

	static final Pattern PASSWORD_PATTERN = Pattern.compile("^[a-f0-9]{40}$");

	static final long MAX_PRICE = 10000L;
	static final long MAX_DIFFICULTY = 4L;
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

	public static long checkFreeChapters(long freeChapters) {
		if (freeChapters < 0 || freeChapters > 100) {
			throw new APIArgumentException("freeChapters");
		}
		return freeChapters;
	}

	public static long checkPrice(long price) {
		return checkPrice(price, "price");
	}

	public static long checkPrice(long price, String name) {
		if (price < 0 || price > MAX_PRICE) {
			throw new APIArgumentException(name);
		}
		return price;
	}

	public static long checkDifficulty(long difficulty) {
		if (difficulty < 0 || difficulty > MAX_DIFFICULTY) {
			throw new APIArgumentException("difficulty");
		}
		return difficulty;
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
		name = name.trim();
		if (name.isEmpty() || name.length() > 100) {
			throw new APIArgumentException("name");
		}
		return name;
	}

	public static String checkDescription(String description) {
		if (description == null) {
			return "";
		}
		description = description.trim();
		if (description.length() > 1000) {
			throw new APIArgumentException("description");
		}
		return description;
	}

	public static String checkContent(String content) {
		if (content != null) {
			content = content.trim();
		}
		// max to 512K:
		if (content == null || content.isEmpty() || content.length() > 524288) {
			throw new APIArgumentException("content");
		}
		return content;
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

	public static long checkDuration(long duration) {
		if (duration <= 0 || duration > 10000) {
			throw new APIArgumentException("duration");
		}
		return duration;
	}

	public static String checkPassword(String password) {
		if (password == null || !PASSWORD_PATTERN.matcher(password).matches()) {
			throw new APIArgumentException("password");
		}
		return password;
	}

	static final LocalDate MIN_DATE = LocalDate.of(2000, 1, 1);
	static final LocalDate MAX_DATE = LocalDate.of(2020, 12, 31);

	public static LocalDate checkLocalDate(String name, LocalDate date) {
		if (date == null || date.isBefore(MIN_DATE) || date.isAfter(MAX_DATE)) {
			throw new APIArgumentException(name);
		}
		return date;
	}

	public static String checkFileName(String fileName) {
		return fileName == null ? "" : fileName;
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

	public static String checkCode(String code, String codeName) {
		if (code == null || code.isEmpty()) {
			return "";
		}
		if (CODE_PATTERN.matcher(code.toLowerCase()).matches()) {
			return code.toLowerCase();
		}
		throw new APIArgumentException(codeName, "invalid code.");
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

}
