package com.github.pister.dbsync.endpoint.remoting.client;

import com.github.pister.dbsync.common.tools.http.HttpClient;
import com.github.pister.dbsync.endpoint.auth.AuthUtil;
import com.github.pister.dbsync.endpoint.remoting.Response;
import com.github.pister.dbsync.endpoint.remoting.AuthToken;
import com.github.pister.dbsync.endpoint.remoting.Request;
import com.github.pister.dbsync.common.tools.util.HessianSerializeUtil;

/**
 * Created by songlihuang on 2017/7/13.
 */
public class DefaultHttpInvoker implements Invoker {

    private ProtocolInvoker protocolInvoker;

    private String remotingUrl;

    private String appId;

    private String appSecret;

    public void init(HttpClient httpClient) {
        HttpClientProtocolInvoker httpClientProtocolInvoker = new HttpClientProtocolInvoker(httpClient);
        httpClientProtocolInvoker.setUrl(remotingUrl);
        this.protocolInvoker = httpClientProtocolInvoker;
    }

    public <T> T createProxy(String serviceName, Class<T> clazz) {
        ProxyServiceFactory proxyServiceFactory = new ProxyServiceFactory(this);
        return proxyServiceFactory.createProxy(serviceName, clazz);
    }

    @Override
    public Response invoke(Request request) {
        try {
            request.setAppId(appId);
            String token = AuthUtil.makeAuthToken(request, appSecret);
            request.setToken(token);
            byte[] requestBytes = HessianSerializeUtil.toBytes(request);
            byte[] responseBytes = protocolInvoker.invoke(requestBytes);
            return (Response) HessianSerializeUtil.toObject(responseBytes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setRemotingUrl(String remotingUrl) {
        this.remotingUrl = remotingUrl;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public void setAppSecret(String appSecret) {
        this.appSecret = appSecret;
    }
}
