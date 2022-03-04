package com.jiudengnile.common.transfer.rt;


import com.jiudengnile.common.transfer.config.Column;
import com.jiudengnile.common.transfer.config.DbConfig;

import java.util.Arrays;
import java.util.List;

/**
 * Created by songlihuang on 2017/7/11.
 */
public class RichSql {

    private String sql;

    private Object[] params;

    private DbConfig dbConfig;

    private List<Column> columns;

    private String pkName = "id";

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

    public DbConfig getDbConfig() {
        return dbConfig;
    }

    public void setDbConfig(DbConfig dbConfig) {
        this.dbConfig = dbConfig;
    }

    public List<Column> getColumns() {
        return columns;
    }

    public void setColumns(List<Column> columns) {
        this.columns = columns;
    }

    public String getPkName() {
        return pkName;
    }

    public void setPkName(String pkName) {
        this.pkName = pkName;
    }

    @Override
    public String toString() {
        return "RichSql{" +
                "sql='" + sql + '\'' +
                ", params=" + Arrays.toString(params) +
                ", dbConfig=" + dbConfig +
                ", columns=" + columns +
                ", pkName='" + pkName + '\'' +
                '}';
    }
}
