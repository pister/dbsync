package com.jiudengnile.common.transfer.http;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

/**
 * Created by songlihuang on 2020/5/28.
 */
public abstract class AbstractHttpResponse {

    private int responseCode;
    private Map<String, List<String>> headers;
    private String defaultCharset;

    public AbstractHttpResponse(String defaultCharset, int responseCode, Map<String, List<String>> headers) {
        this.defaultCharset = defaultCharset;
        this.responseCode = responseCode;
        this.headers = headers;
    }

    public abstract InputStream getResponseStream();

    public abstract byte[] getResponseData();

    public int getResponseCode() {
        return responseCode;
    }

    public String getResponseString() {
        try {
            return new String(getResponseData(), getResponseCharset());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> getHeaders(String name) {
        return headers.get(name);
    }

    public String getHeader(String name) {
        List<String> theHeaders = getHeaders(name);
        if (theHeaders == null || theHeaders.isEmpty()) {
            return null;
        }
        return theHeaders.get(0);
    }


    public String getResponseCharset() {
        String contentType = getHeader("Content-Type");
        if (contentType == null || contentType.length() == 0) {
            return defaultCharset;
        }
        String[] parts = contentType.split(";");
        for (String part : parts) {
            part = part.trim();
            String[] kvParts = part.split("=");
            if (kvParts.length < 2) {
                continue;
            }
            String key = kvParts[0].trim();
            String value = kvParts[1].trim();
            if (key.equals("charset")) {
                return value;
            }
        }
        return defaultCharset;
    }

    @Override
    public String toString() {
        return "HttpResponse{" +
                "responseCode=" + responseCode +
                '}';
    }
}
