package com.github.pister.dbsync.runtime.aop;

/**
 * Created by songlihuang on 2021/3/17.
 */
public class RowInterceptorWrapper {

    private RowInterceptor rowInterceptor;

    private AopContext aopContext;

    public RowInterceptorWrapper(RowInterceptor rowInterceptor, AopContext aopContext) {
        this.rowInterceptor = rowInterceptor;
        this.aopContext = aopContext;
    }

    public RowInterceptor getRowInterceptor() {
        return rowInterceptor;
    }

    public void setRowInterceptor(RowInterceptor rowInterceptor) {
        this.rowInterceptor = rowInterceptor;
    }

    public AopContext getAopContext() {
        return aopContext;
    }

    public void setAopContext(AopContext aopContext) {
        this.aopContext = aopContext;
    }
}
