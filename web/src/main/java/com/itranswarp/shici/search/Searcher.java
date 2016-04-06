package com.itranswarp.shici.search;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileManager.Location;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.itranswarp.shici.util.HttpUtil;
import com.itranswarp.shici.util.HttpUtil.HttpResponse;
import com.itranswarp.shici.util.JsonUtil;
import com.itranswarp.shici.util.MapUtil;
import com.itranswarp.shici.util.ValidateUtil;
import com.itranswarp.warpdb.entity.BaseEntity;

@Component
public class Searcher {

	final Log log = LogFactory.getLog(getClass());

	static final Map<String, String> JSON_HEADERS = MapUtil.createMap("Content-Type", "application/json");

	@Value("${es.url}")
	String esUrl;

	// document ///////////////////////////////////////////////////////////////

	public <T extends BaseEntity> void createDocument(String indexName, T doc) {
		ValidateUtil.checkId(doc.id);
		putJSON(Map.class, indexName + "/" + doc.getClass().getSimpleName() + "/" + doc.id, doc);
	}

	public <T extends BaseEntity> T getDocument(String indexName, Class<T> clazz, String id) {
		ValidateUtil.checkId(id);
		Class<? extends DocumentWrapper<T>> cls = getWrapperClass(clazz);
		log.info(cls);
		return getJSON(cls, indexName + "/" + clazz.getSimpleName() + "/" + id).getDocument();
	}

	// index //////////////////////////////////////////////////////////////////

	public boolean indexExist(String name) {
		try {
			getJSON(Map.class, name);
		} catch (SearchResultException e) {
			return false;
		}
		return true;
	}

	public void createIndex(String name) {
		putJSON(Map.class, name, null);
	}

	public void deleteIndex(String name) {
		deleteJSON(Map.class, name, null);
	}

	// helper /////////////////////////////////////////////////////////////////

	<T> T getJSON(Class<T> clazz, String path) {
		log.info("GET: " + esUrl + path);
		try {
			HttpResponse resp = HttpUtil.httpGet(esUrl + path, null, null);
			return checkResponse(resp, clazz);
		} catch (IOException e) {
			throw new SearchException(e);
		}
	}

	<T> T postJSON(Class<T> clazz, String path, Object data) {
		log.info("POST: " + esUrl + path);
		try {
			HttpResponse resp = HttpUtil.httpPost(esUrl + path, data == null ? null : JsonUtil.toJson(data),
					JSON_HEADERS);
			return checkResponse(resp, clazz);
		} catch (IOException e) {
			throw new SearchException(e);
		}
	}

	<T> T putJSON(Class<T> clazz, String path, Object data) {
		log.info("PUT: " + esUrl + path);
		try {
			HttpResponse resp = HttpUtil.httpPut(esUrl + path, data == null ? null : JsonUtil.toJson(data),
					JSON_HEADERS);
			return checkResponse(resp, clazz);
		} catch (IOException e) {
			throw new SearchException(e);
		}
	}

	<T> T deleteJSON(Class<T> clazz, String path, Object data) {
		log.info("DELETE: " + esUrl + path);
		try {
			HttpResponse resp = HttpUtil.httpDelete(esUrl + path, data == null ? null : JsonUtil.toJson(data),
					JSON_HEADERS);
			return checkResponse(resp, clazz);
		} catch (IOException e) {
			throw new SearchException(e);
		}
	}

	<T> T checkResponse(HttpResponse resp, Class<T> clazz) {
		if (resp.isOK()) {
			log.info("Response: " + resp.body);
			return JsonUtil.fromJson(clazz, resp.body);
		}
		log.info("Error Response: " + resp.body);
		String jsonErr = resp.body;
		if (jsonErr == null) {
			jsonErr = "{}";
		}
		throw JsonUtil.fromJson(SearchResultException.class, jsonErr);
	}

	@SuppressWarnings("unchecked")
	<T extends BaseEntity> Class<? extends DocumentWrapper<T>> getWrapperClass(Class<T> clazz) {
		String T = clazz.getName();
		String packageName = clazz.getPackage().getName();
		String wrapperClassName = "Wrapper_" + clazz.getSimpleName();
		StringBuilder sb = new StringBuilder(256);
		sb.append("package " + packageName + ";\n");
		sb.append("public class " + wrapperClassName + " implements " + DocumentWrapper.class.getName() + "<" + T
				+ "> {\n");
		sb.append("    public String _id;\n");
		sb.append("    public " + T + " _source;\n");
		sb.append("    public " + T + " getDocument() {\n");
		sb.append("        return this._source;\n");
		sb.append("    }\n");
		sb.append("}\n");
		String sourceCode = sb.toString();
		log.info("Generate Java source:\n" + sourceCode);
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		StandardJavaFileManager stdFileManager = compiler.getStandardFileManager(null, null, null);
		JavaFileObject input = new StringInputJavaFileObject(wrapperClassName, sourceCode);
		StringJavaFileManager fileManager = new StringJavaFileManager(stdFileManager);
		CompilationTask task = compiler.getTask(null, fileManager, null, null, null, Arrays.asList(input));
		Boolean result = task.call();
		if (result == null || !result.booleanValue()) {
			throw new RuntimeException("Compilation failed.");
		}
		StringOutputJavaFileObject output = fileManager.output;
		try {
			return (Class<? extends DocumentWrapper<T>>) new CompiledClassLoader(output.getByteCode())
					.loadClass(packageName + "." + wrapperClassName);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

}

class StringInputJavaFileObject extends SimpleJavaFileObject {
	/**
	 * The source code of this "file".
	 */
	final String code;

	StringInputJavaFileObject(String name, String code) {
		super(URI.create("string:///" + name.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
		this.code = code;
	}

	@Override
	public CharSequence getCharContent(boolean ignoreEncodingErrors) {
		return code;
	}
}

class StringOutputJavaFileObject extends SimpleJavaFileObject {

	ByteArrayOutputStream byteCode;

	StringOutputJavaFileObject(final String name, final Kind kind) {
		super(URI.create(name), kind);
	}

	@Override
	public InputStream openInputStream() {
		return new ByteArrayInputStream(getByteCode());
	}

	@Override
	public OutputStream openOutputStream() {
		byteCode = new ByteArrayOutputStream();
		return byteCode;
	}

	/**
	 * @return the byte code generated by the compiler
	 */
	byte[] getByteCode() {
		return byteCode.toByteArray();
	}
}

class StringJavaFileManager extends ForwardingJavaFileManager<JavaFileManager> {

	StringOutputJavaFileObject output;

	public StringJavaFileManager(JavaFileManager fileManager) {
		super(fileManager);
	}

	@Override
	public JavaFileObject getJavaFileForOutput(Location location, String qualifiedName, Kind kind,
			FileObject outputFile) throws IOException {
		output = new StringOutputJavaFileObject(qualifiedName, kind);
		return output;
	}
}

class CompiledClassLoader extends ClassLoader {
	final byte[] classData;

	CompiledClassLoader(byte[] classData) {
		super(CompiledClassLoader.class.getClassLoader());
		this.classData = classData;
	}

	@Override
	protected Class<?> findClass(String qualifiedClassName) throws ClassNotFoundException {
		return defineClass(qualifiedClassName, classData, 0, classData.length);
	}
}
