package com.github.pister.dbsync.endpoint.remoting.client;


import com.github.pister.dbsync.endpoint.remoting.Request;
import com.github.pister.dbsync.endpoint.remoting.Response;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Created by songlihuang on 2017/7/13.
 */
public class ProxyServiceFactory {

    private Invoker invoker;

    public ProxyServiceFactory(Invoker invoker) {
        this.invoker = invoker;
    }

    public <T> T createProxy(final String serviceName, Class<T> clazz) {
        if (!clazz.isInterface()) {
            throw new RuntimeException(clazz +" is not an interface!");
        }
        return (T)Proxy.newProxyInstance(ProxyServiceFactory.class.getClassLoader(), new Class[] {clazz}, new InvocationHandler() {

            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                Request request = new Request();
                String methodName = method.getName();
                request.setMethodName(methodName);
                request.setServiceName(serviceName);
                request.setArgs(args);
                Response response = invoker.invoke(request);
                if (response.getReturnValue() != null) {
                    return response.getReturnValue();
                }
                throw response.getException();
            }
        });
    }

}
