package com.jiudengnile.common.transfer.http;

import wint.lang.utils.IoUtil;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Created by songlihuang on 2020/5/28.
 */
public class HttpLargeBodyResponse extends AbstractHttpResponse implements Closeable {

    private InputStream inputStream;

    public HttpLargeBodyResponse(String defaultCharset, int responseCode, Map<String, List<String>> headers, InputStream inputStream) {
        super(defaultCharset, responseCode, headers);
        this.inputStream = inputStream;
    }

    @Override
    public InputStream getResponseStream() {
        return inputStream;
    }

    @Override
    public byte[] getResponseData() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(1024);
        try {
            IoUtil.copy(inputStream, bos);
            return bos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void close() throws IOException {
        IoUtil.close(inputStream);
    }
}
