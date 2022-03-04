package com.github.pister.dbsync.http;


import com.github.pister.dbsync.http.multipart.Part;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * User: huangsongli
 * Date: 16/3/6
 * Time: 下午7:33
 */
public interface HttpClient {

    HttpLargeBodyResponse doRequestForLargeBody(HttpMethod method, String url, Map<String, String> userHeaders, InputStream data) throws IOException;

    HttpResponse doRequest(HttpMethod method, String url, Map<String, String> userHeaders, InputStream data) throws IOException;

    byte[] doGet(String url) throws IOException;

    HttpResponse doGet(String url, Map<String, String> headers) throws IOException;

    String doGetForString(String url) throws IOException;

    HttpResponse doPost(String url, Map<String, String> headers, InputStream data) throws IOException;

    HttpResponse doMultiPartsRequest(String url, Map<String, String> userHeaders, List<Part> parts) throws IOException;

    String doPostForString(String url, InputStream data) throws IOException;

    HttpResponse doFormPost(String url, Map<String, Object> formQueryString);

    HttpResponse doFormPost(String url, Map<String, String> headers, Map<String, Object> formQueryString);

    String doFormPostForString(String url, Map<String, Object> formQueryString);
}
