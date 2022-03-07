package com.github.pister.dbsync.endpoint.remoting.client;

import com.github.pister.dbsync.common.Constants;
import com.github.pister.dbsync.common.tools.http.HttpClient;
import com.github.pister.dbsync.common.tools.http.JavaHttpClient;
import com.github.pister.dbsync.common.tools.util.HessianSerializeUtil;
import com.github.pister.dbsync.endpoint.auth.AuthUtil;
import com.github.pister.dbsync.endpoint.remoting.Request;
import com.github.pister.dbsync.endpoint.remoting.Response;
import com.github.pister.dbsync.endpoint.server.DbSyncServer;

/**
 * Created by songlihuang on 2017/7/13.
 */
public class RemoteDbSyncServerFactory {

    private ProtocolInvoker protocolInvoker;

    private String remotingUrl;

    private String appId;

    private String appSecret;

    private HttpClient httpClient = new JavaHttpClient();

    private final Invoker invoker = new Invoker() {
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
    };

    public void init() {
        HttpClientProtocolInvoker httpClientProtocolInvoker = new HttpClientProtocolInvoker(httpClient);
        httpClientProtocolInvoker.setUrl(remotingUrl);
        this.protocolInvoker = httpClientProtocolInvoker;
    }

    public DbSyncServer createRemoteDbSyncServer() {
        ProxyServiceFactory proxyServiceFactory = new ProxyServiceFactory(invoker);
        return proxyServiceFactory.createProxy(Constants.DB_SYNC_SERVICE_NAME, DbSyncServer.class);
    }

    public void setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
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
