package com.jiudengnile.common.transfer.remoting.client;

import com.jiudengnile.common.transfer.http.HttpClient;
import com.jiudengnile.common.transfer.remoting.AuthToken;
import com.jiudengnile.common.transfer.remoting.Request;
import com.jiudengnile.common.transfer.remoting.Response;
import com.jiudengnile.common.transfer.util.HessianSerializeUtil;

/**
 * Created by songlihuang on 2017/7/13.
 */
public class DefaultHttpInvoker implements Invoker {

    private ProtocolInvoker protocolInvoker;

    private String remotingUrl;

    private String authToken = AuthToken.VALUE;

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
            request.setAuthToken(authToken);
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


}
