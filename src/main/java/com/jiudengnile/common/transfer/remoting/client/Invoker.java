package com.jiudengnile.common.transfer.remoting.client;


import com.jiudengnile.common.transfer.remoting.Request;
import com.jiudengnile.common.transfer.remoting.Response;

/**
 * Created by songlihuang on 2017/7/13.
 */
public interface Invoker {

    Response invoke(Request request);

}
