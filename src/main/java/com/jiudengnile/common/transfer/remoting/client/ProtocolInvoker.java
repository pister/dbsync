package com.jiudengnile.common.transfer.remoting.client;

import java.io.IOException;

/**
 * Created by songlihuang on 2017/7/13.
 */
public interface ProtocolInvoker {

    byte[] invoke(byte[] request) throws IOException;
}
