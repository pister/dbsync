package com.github.pister.dbsync.endpoint.remoting.server;

import com.github.pister.dbsync.common.Constants;
import com.github.pister.dbsync.common.tools.util.MapUtil;
import com.github.pister.dbsync.common.tools.util.StringUtil;
import com.github.pister.dbsync.endpoint.auth.AppSecretProvider;
import com.github.pister.dbsync.endpoint.auth.AuthUtil;
import com.github.pister.dbsync.endpoint.auth.MapAppSecretProvider;
import com.github.pister.dbsync.endpoint.remoting.Request;
import com.github.pister.dbsync.endpoint.remoting.Response;
import com.github.pister.dbsync.endpoint.server.DefaultSyncServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Map;

/**
 * Created by songlihuang on 2017/7/13.
 */
public class ServiceExporter {

    private static final Logger log = LoggerFactory.getLogger(ServiceExporter.class);

    private Map<String, Method> methodsMap = MapUtil.newConcurrentHashMap();

    private DefaultSyncServer dbSyncServer = new DefaultSyncServer();

    private Map<String, Object> namedServices = MapUtil.newHashMap();

    private AppSecretProvider appSecretProvider;

    public ServiceExporter(AppSecretProvider appSecretProvider) {
        this.appSecretProvider = appSecretProvider;
    }

    public ServiceExporter(Map<String, String> appSecretMap) {
        this(new MapAppSecretProvider(appSecretMap));
    }


    private void registerService(String serviceName, Object service) {
        namedServices.put(serviceName, service);
    }

    /**
     * 注册数据库配置
     * @param dbIndex
     * @param shortUrl
     * @param username
     * @param password
     */
    public void registerDbConfig(int dbIndex, String shortUrl, String username, String password) {
        dbSyncServer.registerDbConfig(dbIndex, shortUrl, username, password);
    }

    /**
     * 设置最后修改时间字段
     * @param dbIndex
     * @param tableName
     * @param lastModifiedField
     */
    public void setTableLastModifiedField(int dbIndex, String tableName, String lastModifiedField) {
        dbSyncServer.setTableLastModifiedField(dbIndex, tableName, lastModifiedField);
    }

    public void init() {
        try {
            dbSyncServer.init();
            registerService(Constants.DB_SYNC_SERVICE_NAME, dbSyncServer);
        } catch (Exception e) {
            log.error("init error", e);
            throw new RuntimeException(e);
        }
    }

    private Response checkForAuth(Request request) {
        String appId = request.getAppId();
        if (StringUtil.isEmpty(appId)) {
            return createThrowableResponse(new Exception("miss appId"));
        }
        String appSecret = appSecretProvider.getAppSecret(request.getAppId());
        String tokenByRequest = request.getToken();
        String tokenByGen = AuthUtil.makeAuthToken(request, appSecret);
        if (!StringUtil.equals(tokenByRequest, tokenByGen)) {
            return createThrowableResponse(new Exception("token not match"));
        }
        return null;
    }

    public Response invoke(Request request) {
        Response resp = checkForAuth(request);
        if (resp != null) {
            return resp;
        }
        Object service = namedServices.get(request.getServiceName());
        if (service == null) {
            return createThrowableResponse(new Exception("service not exist:" + request.getServiceName()));
        }
        try {
            // 这里没有判断方法重载
            final Method method = getMethod(service.getClass(), request.getMethodName());
            Object returnValue = AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
                try {
                    return method.invoke(service, request.getArgs());
                } catch (InvocationTargetException e) {
                    throw new RuntimeException(e.getTargetException());
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            });
            Response response = new Response();
            response.setReturnValue(returnValue);
            return response;
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private Method getMethod(Class serviceClass, String methodName) throws NoSuchMethodException {
        Method cachedMethod = methodsMap.get(methodName);
        if (cachedMethod != null) {
            return cachedMethod;
        } else {
            Method[] methods = serviceClass.getMethods();
            for (Method m : methods) {
                if (m.getName().equals(methodName)) {
                    methodsMap.put(methodName, m);
                    return m;
                }
            }
        }
        throw new NoSuchMethodException("not found method:" + methodName);
    }

    private Response createThrowableResponse(Throwable t) {
        Response response = new Response();
        response.setException(t);
        return response;
    }

}
