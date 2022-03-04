package com.github.pister.dbsync.endpoint.remoting;

import java.io.Serializable;

/**
 * Created by songlihuang on 2017/7/13.
 */
public class Request implements Serializable {

    private String authToken;

    private String serviceName;

    private String methodName;

    private Object[] args;

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }
}
