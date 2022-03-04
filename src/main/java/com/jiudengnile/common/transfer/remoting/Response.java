package com.jiudengnile.common.transfer.remoting;

import java.io.Serializable;

/**
 * Created by songlihuang on 2017/7/13.
 */
public class Response implements Serializable {

    private static final long serialVersionUID = -117739194884729237L;
    private Object returnValue;

    private Throwable exception;

    public Object getReturnValue() {
        return returnValue;
    }

    public void setReturnValue(Object returnValue) {
        this.returnValue = returnValue;
    }

    public Throwable getException() {
        return exception;
    }

    public void setException(Throwable exception) {
        this.exception = exception;
    }
}
