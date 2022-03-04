
package com.github.pister.dbsync.common.tools.util;



import com.github.pister.dbsync.common.io.FastByteArrayOutputStream;
import com.github.pister.dbsync.common.io.FastByteArrayInputStream;

import java.io.*;

public class FileUtil {
	
	public static boolean existFile(String path) {
		File f = new File(path);
		if (!f.isFile()) {
			return false;
		}
		return f.exists();
	}
	
	public static boolean existDir(String path) {
		File f = new File(path);
		if (!f.isDirectory()) {
			return false;
		}
		return f.exists();
	}
	
	
	public static boolean exist(String path) {
		File f = new File(path);
		return f.exists();
	}
	
	public static String getFileExtension(String name) {
		if (StringUtil.isEmpty(name)) {
			return StringUtil.EMPTY;
		}
		int pos = name.lastIndexOf('.');
		if (pos < 0) {
			return StringUtil.EMPTY;
		}
		return name.substring(pos + 1);
	}

	public static byte[] readContent(File file) throws IOException {
		FileInputStream is = new FileInputStream(file);
		FastByteArrayOutputStream bos = new FastByteArrayOutputStream();
		IoUtil.copyAndClose(is, bos);
		return bos.toByteArray();
	}
	
	public static void writeContent(File file, InputStream is) throws IOException {
		FileOutputStream os = new FileOutputStream(file);
		IoUtil.copyAndClose(is, os);
	}
	
	public static void writeContent(File file, byte[] bytes) throws IOException {
		FastByteArrayInputStream bis = new FastByteArrayInputStream(bytes);
		writeContent(file, bis);
	}
	
	public static void writeContent(File file, String str, String charset) throws IOException {
		if (str == null) {
			return;
		}
		writeContent(file, str.getBytes(charset));
	}
	
	public static void writeContent(File file, String str) throws IOException {
		if (str == null) {
			return;
		}
		writeContent(file, str.getBytes());
	}
	
	public static String readAsString(File file) throws IOException {
		return new String(readContent(file));
	}
	
	public static String readAsString(File file, String charset) throws IOException {
		return new String(readContent(file), charset);
	}
	
}
