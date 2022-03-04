package com.github.pister.dbsync.common.tools.util;

import java.io.*;

/**
 * User: huangsongli
 * Date: 16/2/26
 * Time: 下午9:59
 */
public class IoUtil {

    private static final int BUF_LEN = 1024 * 4;
    /**
     * 通过threadLocal做cache优化，避免重复申请内存
     */
    private static final ThreadLocal<byte[]> bufTl = new ThreadLocal<byte[]>() {
        @Override
        protected byte[] initialValue() {
            return new byte[BUF_LEN];
        }
    };

    private static byte[] getBuf() {
        return bufTl.get();
    }

    public static void copyAndClose(InputStream is, OutputStream os) throws IOException {
        copy(is, os);
        close(is);
        close(os);
    }

    public static void copy(InputStream is, OutputStream os) throws IOException {
        byte[] buf = getBuf();
        while (true) {
            int len = is.read(buf);
            if (len < 0) {
                break;
            }
            os.write(buf, 0, len);
        }
    }

    public static byte[] readAsBytes(File file) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(BUF_LEN);
        copyAndClose(readAsStream(file), bos);
        return bos.toByteArray();
    }

    public static void writeBytesToFile(File file, byte[] data) throws IOException{
        FileOutputStream fos = new FileOutputStream(file);
        copyAndClose(new ByteArrayInputStream(data), fos);
    }

    public static InputStream readAsStream(File file) throws IOException {
        return new FileInputStream(file);
    }

    public static void close(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (Exception e) {
                // ignore
            }
        }
    }

    public static String getFileNameSuffix(String name) {
        if (name == null) {
            return null;
        }
        int pos = name.lastIndexOf('.');
        if (pos < 0) {
            return null;
        }
        return name.substring(pos);
    }

}
