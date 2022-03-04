package com.github.pister.dbsync.endpoint.remoting.server;

import com.github.pister.dbsync.common.Constants;
import com.github.pister.dbsync.endpoint.server.DefaultDbSyncServer;
import com.github.pister.dbsync.endpoint.remoting.Request;
import com.github.pister.dbsync.endpoint.remoting.Response;
import com.github.pister.dbsync.common.tools.util.StringUtil;
import com.github.pister.dbsync.config.DbConfig;
import com.github.pister.dbsync.endpoint.remoting.AuthToken;
import com.github.pister.dbsync.common.tools.util.MapUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.List;
import java.util.Map;

/**
 * Created by songlihuang on 2017/7/13.
 */
public class ServiceExporter {

    private static final Logger log = LoggerFactory.getLogger(ServiceExporter.class);

    private Map<String, Object> namedServices = MapUtil.newHashMap();

    /**
     * format:  hostname1:dbname1, hostname2:dbname2, ...
     */
    private String databases;

    private void registerService(String serviceName, Object service) {
        namedServices.put(serviceName, service);
    }

    private DbConfig makeDbConfig(String hostname, String dbName) {
        DbConfig dbConfig = new DbConfig();
        dbConfig.setDbName(dbName);
        dbConfig.setUsername("xdata_user");
        dbConfig.setPassword("xdata_pwd");
        dbConfig.setUrl("jdbc:mysql://" + hostname + ":3306/" + dbName + "?useUnicode=true&amp;characterEncoding=utf8&amp;zeroDateTimeBehavior=convertToNull&amp;transformedBitIsBoolean=true");
        return dbConfig;
    }


    public void init() {
        try {
            DefaultDbSyncServer defaultTransferServer = new DefaultDbSyncServer();

            List<String> dbList = StringUtil.splitTrim(databases, ",");
            int index = 0;
            for (String dbPair : dbList) {
                String[] parts = dbPair.split(":");
                defaultTransferServer.registerDbConfig(index, makeDbConfig(parts[0], parts[1]));
                index++;
            }

            //  defaultTransferServer.registerDbConfig(makeDbConfig("127.0.0.1", "xdata_00"))
            //  defaultTransferServer.registerDbConfig(makeDbConfig("127.0.0.1", "xdata_01"));

            defaultTransferServer.setTableUpdatedFieldField(0, "sequences", "last_modified");
            defaultTransferServer.init();

            registerService(Constants.DB_SYNC_SERVICE_NAME, defaultTransferServer);
        } catch (Exception e) {
            log.error("init error", e);
            throw new RuntimeException(e);
        }
    }

    public Response invoke(Request request) {
        String authToken = request.getAuthToken();
        if (!StringUtil.equals(AuthToken.VALUE, authToken)) {
            return createThrowableResponse(new Exception("bad auth token error"));
        }
        Object service = namedServices.get(request.getServiceName());
        if (service == null) {
            return createThrowableResponse(new Exception("service not exist:" + request.getServiceName()));
        }
        try {
            // 这里没有判断方法重载
            final Method method = service.getClass().getMethod(request.getMethodName());
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

    private Response createThrowableResponse(Throwable t) {
        Response response = new Response();
        response.setException(t);
        return response;
    }

    public void setDatabases(String databases) {
        this.databases = databases;
    }
}
