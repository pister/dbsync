package com.github.pister.dbsync.http;

import com.github.pister.dbsync.http.multipart.MultiPartConstants;
import com.github.pister.dbsync.http.multipart.Part;
import com.github.pister.dbsync.io.FastByteArrayInputStream;
import com.github.pister.dbsync.util.MapUtil;
import com.github.pister.dbsync.util.StringUtil;
import com.github.pister.dbsync.util.assist.Transformer;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;
import java.io.*;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: huangsongli
 * Date: 16/3/6
 * Time: 下午7:37
 */
public abstract class AbstractHttpClient implements HttpClient {

    protected String defaultCharset = "utf-8";

    protected int timeout = 5000;
    protected boolean useProxy;
    protected String hostname;
    protected int port;

    @Override
    public byte[] doGet(String url) throws IOException {
        return doGet(url, null).getResponseData();
    }

    @Override
    public HttpResponse doGet(String url, Map<String, String> headers) throws IOException {
        return doRequest(HttpMethod.GET, url, headers, null);
    }

    @Override
    public String doGetForString(String url) throws IOException {
        try {
            HttpResponse response = doGet(url, null);
            return new String(response.getResponseData(), response.getResponseCharset());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public HttpResponse doPost(String url, Map<String, String> headers, InputStream data) throws IOException {
        return doRequest(HttpMethod.POST, url, headers, data);
    }

    @Override
    public abstract HttpResponse doRequest(HttpMethod method, String url, Map<String, String> userHeaders, InputStream data) throws IOException;

    @Override
    public HttpResponse doMultiPartsRequest(String url, Map<String, String> userHeaders, List<Part> parts) throws IOException {
        Map<String, String> headers = MapUtil.newHashMap();
        if (userHeaders != null) {
            headers.putAll(userHeaders);
        }
        headers.put("Content-Type", "multipart/form-data; boundary=" + MultiPartConstants.BOUNDARY);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(8192);
        for (Part part : parts) {
           part.writeTo(outputStream);
        }
        outputStream.write(MultiPartConstants.EXTRA_BYTES);
        outputStream.write(MultiPartConstants.BOUNDARY_BYTES);
        outputStream.write(MultiPartConstants.EXTRA_BYTES);

        outputStream.close();
        return doRequest(HttpMethod.POST, url, headers, new FastByteArrayInputStream(outputStream.toByteArray()));
    }


    @Override
    public String doPostForString(String url, InputStream data) throws IOException {
        HttpResponse response = doPost(url, null, data);
        return response.getResponseString();
    }

    @Override
    public HttpResponse doFormPost(String url, Map<String, Object> formQueryString) {
        return doFormPost(url, new HashMap<String, String>(), formQueryString);
    }

    private byte[] toFormData(Map<String, Object> formQueryString) throws UnsupportedEncodingException {
        String formData = MapUtil.join(formQueryString, "=", "&", new Transformer<Object, String>() {
            @Override
            public String transform(Object object) {
                if (object == null) {
                    return StringUtil.EMPTY;
                }
                try {
                    return URLEncoder.encode(object.toString(), defaultCharset);
                } catch (UnsupportedEncodingException e) {
                    return object.toString();
                }
            }
        });
        return formData.getBytes(defaultCharset);
    }

    @Override
    public HttpResponse doFormPost(String url, Map<String, String> headers, Map<String, Object> formQueryString) {
        Map<String, String> newHeaders = MapUtil.newHashMap(headers);
        newHeaders.put("Content-Type", "application/x-www-form-urlencoded;charset=" + defaultCharset);
        try {
            byte[] data = toFormData(formQueryString);
            return doPost(url, newHeaders, new ByteArrayInputStream(data));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String doFormPostForString(String url, Map<String, Object> formQueryString) {
        HttpResponse response = doFormPost(url, formQueryString);
        return response.getResponseString();
    }

    protected final SecureRandom secureRandom = new SecureRandom();

    protected static class TrustAnyTrustManager implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }

    protected static class TrustAnyHostnameVerifier implements HostnameVerifier {
        @Override
        public boolean verify(String s, SSLSession sslSession) {
            return true;
        }
    }

    public String getDefaultCharset() {
        return defaultCharset;
    }

    public void setDefaultCharset(String defaultCharset) {
        this.defaultCharset = defaultCharset;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public boolean isUseProxy() {
        return useProxy;
    }

    public void setUseProxy(boolean useProxy) {
        this.useProxy = useProxy;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
