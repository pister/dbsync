package com.jiudengnile.common.transfer.remoting.server;

import com.jiudengnile.common.transfer.DefaultTransferServer;
import com.jiudengnile.common.transfer.config.DbConfig;
import com.jiudengnile.common.transfer.remoting.AuthToken;
import com.jiudengnile.common.transfer.remoting.Request;
import com.jiudengnile.common.transfer.remoting.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wint.lang.exceptions.CanNotFindMethodException;
import wint.lang.magic.MagicObject;
import wint.lang.utils.MapUtil;
import wint.lang.utils.StringUtil;

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
            DefaultTransferServer defaultTransferServer = new DefaultTransferServer();

            List<String> dbList = StringUtil.splitTrim(databases, ",");
            for (String dbPair : dbList) {
                String[] parts = dbPair.split(":");
                defaultTransferServer.addDbConfig(makeDbConfig(parts[0], parts[1]));
            }

            //  defaultTransferServer.addDbConfig(makeDbConfig("127.0.0.1", "xdata_00"))
            //  defaultTransferServer.addDbConfig(makeDbConfig("127.0.0.1", "xdata_01"));

            defaultTransferServer.setTableUpdatedFieldField(0, "sequences", "last_modified");
            defaultTransferServer.init();

            registerService("transferServer", defaultTransferServer);
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
        MagicObject magicObject = MagicObject.wrap(service);
        try {
            Object returnValue = magicObject.invoke(request.getMethodName(), request.getArgs());
            Response response = new Response();
            response.setReturnValue(returnValue);
            return response;
        } catch (CanNotFindMethodException e) {
            return createThrowableResponse(e);
        } catch (Throwable t) {
            return createThrowableResponse(t);
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
