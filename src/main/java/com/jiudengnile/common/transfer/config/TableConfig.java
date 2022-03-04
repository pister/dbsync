package com.jiudengnile.common.transfer.config;

/**
 * Created by songlihuang on 2017/7/11.
 */
public class TableConfig {

    private DbConfig dbConfig;

    private String tableName;

    private String updatedField = "gmt_modified";

    public DbConfig getDbConfig() {
        return dbConfig;
    }

    public void setDbConfig(DbConfig dbConfig) {
        this.dbConfig = dbConfig;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getUpdatedField() {
        return updatedField;
    }

    public void setUpdatedField(String updatedField) {
        this.updatedField = updatedField;
    }

}
