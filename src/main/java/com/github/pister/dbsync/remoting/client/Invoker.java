package com.github.pister.dbsync.remoting.client;


import com.github.pister.dbsync.remoting.Request;
import com.github.pister.dbsync.remoting.Response;

/**
 * Created by songlihuang on 2017/7/13.
 */
public interface Invoker {

    Response invoke(Request request);

}
