package com.itranswarp.shici.store;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.itranswarp.warpdb.IdUtil;

public abstract class AbstractStorage {

	static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd/HH/");

	public abstract String toUrl(RemoteObject ro);

	public abstract String toInternalUrl(RemoteObject ro);

	public abstract String toImageUrl(RemoteObject ro, String style);

	public RemoteObject put(String fileExt, byte[] data) throws IOException {
		return put(null, fileExt, data);
	}

	public RemoteObject put(String fileExt, InputStream input) throws IOException {
		return put(null, fileExt, input);
	}

	public RemoteObject put(String prefix, String fileExt, byte[] data) throws IOException {
		return put(prefix, fileExt, new ByteArrayInputStream(data));
	}

	public abstract RemoteObject put(String prefix, String fileExt, InputStream input) throws IOException;

	public String generateFileName(String prefix, String ext) {
		LocalDateTime dt = LocalDateTime.now();
		if (prefix == null) {
			return dt.format(FORMATTER) + IdUtil.next() + ext;
		} else {
			return prefix + "/" + dt.format(FORMATTER) + IdUtil.next() + ext;
		}
	}
}
