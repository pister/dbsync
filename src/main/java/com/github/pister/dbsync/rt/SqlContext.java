package com.github.pister.dbsync.rt;

/**
 * Created by songlihuang on 2017/7/11.
 */
public class SqlContext {

    private String sql;
    private Object[] params;

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public Object[] getParams() {
        return params;
    }

    public void setParams(Object[] params) {
        this.params = params;
    }

}
