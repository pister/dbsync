package com.github.pister.dbsync.remoting.client;


import com.github.pister.dbsync.http.HttpClient;
import com.github.pister.dbsync.http.HttpMethod;
import com.github.pister.dbsync.http.HttpResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Created by songlihuang on 2017/7/13.
 */
public class HttpClientProtocolInvoker implements ProtocolInvoker {

    private HttpClient httpClient;

    private String url;

    public HttpClientProtocolInvoker(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public byte[] invoke(byte[] request) throws IOException {
        HttpResponse httpResponse = httpClient.doRequest(HttpMethod.POST, url, null, new ByteArrayInputStream(request));
        if (httpResponse.getResponseCode() != 200) {
            throw new RuntimeException("invoke error, http code:" + httpResponse.getResponseCode());
        }
        return httpResponse.getResponseData();
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
