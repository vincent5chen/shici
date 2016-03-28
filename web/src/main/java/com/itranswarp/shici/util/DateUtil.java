package com.itranswarp.shici.util;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class DateUtil {

	static final DateTimeFormatter RFC822_FORMATTER = DateTimeFormatter.ofPattern("EEE, dd MMM uuuu HH:mm:ss 'GMT'",
			Locale.US);

	static final DateTimeFormatter RFC8601_FORMATTER = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss'Z'",
			Locale.US);

	/**
	 * Return RFC822 GMT date string like "Wed, 30 Sep 2015 07:00:04 GMT".
	 * 
	 * @return
	 */
	public static String gmtNow() {
		ZonedDateTime dt = ZonedDateTime.now(ZoneOffset.UTC);
		return dt.format(RFC822_FORMATTER);
	}

	/**
	 * Return ISO8601 date string like "2015-09-30T07:00:04Z".
	 * @return
	 */
	public static String isoNow(){
		ZonedDateTime dt = ZonedDateTime.now(ZoneOffset.UTC);
		return dt.format(RFC8601_FORMATTER);
	}
}
