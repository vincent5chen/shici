package com.itranswarp.shici.store;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.itranswarp.shici.util.DateUtil;
import com.itranswarp.shici.util.HashUtil;
import com.itranswarp.shici.util.HttpUtil;

@Component
public class AliyunStorage extends AbstractStorage {

	@Value("${storage.resource.location}")
	String location;

	@Value("${storage.resource.host}")
	String host;

	@Value("${storage.resource.internal.host}")
	String internalHost;

	@Value("${storage.resource.bucket}")
	String bucket;

	@Value("${storage.resource.access_key_id}")
	String accessKeyId;

	@Value("${storage.resource.access_key_secret}")
	String accessKeySecret;

	@Override
	public String toUrl(RemoteObject ro) {
		return "//" + ro.bucket + ".oss-" + ro.location + ".aliyuncs.com/" + ro.object;
	}

	@Override
	public String toInternalUrl(RemoteObject ro) {
		return "//" + ro.bucket + ".oss-" + ro.location + "-internal.aliyuncs.com/" + ro.object;
	}

	@Override
	public String toImageUrl(RemoteObject ro, String style) {
		return "//" + ro.bucket + ".img-" + ro.location + ".aliyuncs.com/" + ro.object + "@" + style;
	}

	@Override
	public RemoteObject put(String prefix, String ext, InputStream input) throws IOException {
		String contentType = HttpUtil.guessContentType(ext);
		String now = DateUtil.gmtNow();
		String fileName = generateFileName(prefix, ext);
		String signature = sign("PUT", contentType, now, this.bucket, fileName);
		Map<String, String> headers = new HashMap<>();
		headers.put("Date", now);
		headers.put("Content-Type", contentType);
		headers.put("Content-Disposition", "inline");
		headers.put("Cache-Control", "max-age=31536000");
		headers.put("Authorization", "OSS " + accessKeyId + ":" + signature);
		HttpUtil.httpPut("http://" + internalHost + "/" + fileName, input, headers);
		return new RemoteObject(location, bucket, fileName);
	}

	public void delete(String url) throws IOException {
		String prefix = "//" + host + "/";
		if (!url.startsWith(prefix)) {
			throw new IOException("Cannot delete url: " + url);
		}
		String now = DateUtil.gmtNow();
		String fileName = url.substring(prefix.length());
		String signature = sign("DELETE", "", now, this.bucket, fileName);
		Map<String, String> headers = new HashMap<>();
		headers.put("Date", now);
		headers.put("Authorization", "OSS " + accessKeyId + ":" + signature);
		HttpUtil.httpDelete("http://" + internalHost + "/" + fileName, "", headers);
	}

	String sign(String method, String contentType, String gmtDate, String bucket, String fileName) {
		String[] ss = new String[] { method, "", contentType, gmtDate, "/" + bucket + "/" + fileName };
		String s = String.join("\n", ss);
		return HashUtil.hmacSha1(s, accessKeySecret);
	}
}
