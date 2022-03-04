package com.jiudengnile.common.transfer.http;



import com.jiudengnile.common.transfer.util.IoUtil;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: huangsongli
 * Date: 16/2/26
 * Time: 下午9:34
 */
public class JavaHttpClient extends AbstractHttpClient implements HttpClient {


    private HttpURLConnection openConnection(URL url) throws IOException {
        HttpURLConnection ret;
        if (isUseProxy()) {
            ret = (HttpURLConnection) url.openConnection(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(getHostname(), getPort())));
        } else {
            ret = (HttpURLConnection) url.openConnection();
        }

        //  return new YbHttpURLConnection(url, ret);
        return ret;
    }

    @Override
    public HttpLargeBodyResponse doRequestForLargeBody(HttpMethod method, String url, Map<String, String> userHeaders, InputStream data) throws IOException {
        HttpURLConnection urlConnection = handleRequest(method, url, userHeaders, data);
        InputStream responseInputStream = urlConnection.getInputStream();
        int responseCode = urlConnection.getResponseCode();
        Map<String, List<String>> headers = new HashMap<String, List<String>>(urlConnection.getHeaderFields());
        return new HttpLargeBodyResponse(defaultCharset, responseCode, headers, responseInputStream);
    }

    private HttpURLConnection handleRequest(HttpMethod method, String url, Map<String, String> userHeaders, InputStream data) throws IOException {
        URL urlObject = new URL(url);
        HttpURLConnection urlConnection = openConnection(urlObject);
        if (urlObject.getProtocol().equalsIgnoreCase("https")) {
            HttpsURLConnection httpsURLConnection = (HttpsURLConnection) urlConnection;
            prepareForHttps(httpsURLConnection);
        }
        urlConnection.setRequestMethod(method.name());
        urlConnection.setConnectTimeout(timeout);
        urlConnection.setReadTimeout(timeout);
        urlConnection.setInstanceFollowRedirects(false);
        if (userHeaders != null) {
            for (Map.Entry<String, String> entry : userHeaders.entrySet()) {
                urlConnection.addRequestProperty(entry.getKey(), entry.getValue());
            }
        }
        if (data != null) {
            int len = data.available();
            urlConnection.addRequestProperty("Content-Length", String.valueOf(len));
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            OutputStream outputStream = urlConnection.getOutputStream();
            IoUtil.copyAndClose(data, outputStream);
        }
        return urlConnection;
    }

    public HttpResponse doRequest(HttpMethod method, String url, Map<String, String> userHeaders, InputStream data) throws IOException {
        HttpURLConnection urlConnection = handleRequest(method, url, userHeaders, data);
        InputStream responseInputStream = urlConnection.getInputStream();
        int responseCode = urlConnection.getResponseCode();
        ByteArrayOutputStream bos = new ByteArrayOutputStream(64);
        IoUtil.copyAndClose(responseInputStream, bos);
        Map<String, List<String>> headers = new HashMap<String, List<String>>(urlConnection.getHeaderFields());
        return new HttpResponse(defaultCharset, responseCode, headers, bos.toByteArray());
    }

    private void prepareForHttps(HttpsURLConnection httpsURLConnection) {
        try {
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, new TrustManager[]{new TrustAnyTrustManager()}, secureRandom);
            httpsURLConnection.setSSLSocketFactory(sslContext.getSocketFactory());
            httpsURLConnection.setHostnameVerifier(new TrustAnyHostnameVerifier());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (KeyManagementException e) {
            throw new RuntimeException(e);
        }
    }

}
