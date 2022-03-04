package com.github.pister.dbsync.common.tools.http;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * User: huangsongli
 * Date: 16/3/6
 * Time: 下午7:38
 */
public class HttpResponse extends AbstractHttpResponse{

    private byte[] responseData;

    public HttpResponse(String defaultCharset, int responseCode, Map<String, List<String>> headers, byte[] responseData) {
        super(defaultCharset, responseCode, headers);
        this.responseData = responseData;
    }

    public InputStream getResponseStream() {
        return new ByteArrayInputStream(responseData);
    }

    public byte[] getResponseData() {
        return responseData;
    }
}
