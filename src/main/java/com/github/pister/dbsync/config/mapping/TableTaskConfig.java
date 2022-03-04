package com.github.pister.dbsync.config.mapping;

import com.github.pister.dbsync.runtime.aop.BatchInterceptor;
import com.github.pister.dbsync.runtime.aop.RowInterceptor;
import com.github.pister.dbsync.common.tools.util.MapUtil;

import java.util.Map;

/**
 * Created by songlihuang on 2021/1/24.
 */
public class TableTaskConfig extends RichTableConfig {

    private String taskName;

    private int remoteDbIndex;

    private String remoteTable;

    private String updatedField = "gmt_modified";

    /**
     * 只跑全量，不跑增量
     */
    private boolean onlyFullDump = false;

    /**
     * 来源表额外条件，只支持固定条件，不支持参数注入。
     *
     * 开头不用些and和where，最后拼装会以如下方式拼装;
     *
     * 有where的情况
     * select * from table1 where id > ? order by id desc limit 20;
     * =>
     * select * from table1 where id > ? AND $sqlCondition$ order by id desc limit 20;
     *
     * 没有where的情况
     * select * from table1 order by id desc limit 20;
     * =>
     * select * from table1 WHERE $sqlCondition$ order by id desc limit 20;
     */
    private String sourceExtCondition;

    /**
     * 对每一条记录都会进行拦截(批量)
     */
    private BatchInterceptor batchInterceptor;

    /**
     * 对每一条记录都会进行拦截（逐条）
     */
    private RowInterceptor rowInterceptor;

    /**
     * 表字段映射，默认情况下目标表会从来源表同名字段中复制，该字段用于字段名不一样的情况
     */
    private Map<String, String> /* local ==> remote */ columnNameMapping;

    /**
     * 创建一个单表的迁移
     *
     * @param taskName
     * @param sourceDbIndex
     * @param sourceTable
     * @param destDbIndex
     * @param destTable
     * @return
     */
    public static TableTaskConfig makeSingle(String taskName, int sourceDbIndex, String sourceTable, int destDbIndex, String destTable) {
        TableTaskConfig tableTaskConfig = new TableTaskConfig();
        tableTaskConfig.setShardSupport(false);
        tableTaskConfig.setTaskName(taskName);
        tableTaskConfig.setRemoteDbIndex(sourceDbIndex);
        tableTaskConfig.setRemoteTable(sourceTable);
        tableTaskConfig.setSingleTableDbIndex(destDbIndex);
        tableTaskConfig.setTableName(destTable);
        return tableTaskConfig;
    }

    public static TableTaskConfig makeOneTooManyShard(String taskName, int sourceDbIndex, String sourceTable, String destTableFormat,
                                                      String routeColumn, int dbCount, int tableCountPerDb) {
        TableTaskConfig tableTaskConfig = new TableTaskConfig();
        tableTaskConfig.setShardSupport(true);
        tableTaskConfig.setTaskName(taskName);
        tableTaskConfig.setRemoteDbIndex(sourceDbIndex);
        tableTaskConfig.setRemoteTable(sourceTable);
        tableTaskConfig.setTableName(destTableFormat);
        tableTaskConfig.setRouteColumn(routeColumn);
        tableTaskConfig.setDbCount(dbCount);
        tableTaskConfig.setTableCountPerDb(tableCountPerDb);
        return tableTaskConfig;
    }


    public TableTaskConfig mappingColumn(String dest, String source) {
        if (columnNameMapping == null) {
            columnNameMapping = MapUtil.newHashMap();
        }
        columnNameMapping.put(dest, source);
        return this;
    }


    public TableTaskConfig batchInterceptor(BatchInterceptor batchInterceptor) {
        this.batchInterceptor = batchInterceptor;
        return this;
    }

    public BatchInterceptor getBatchInterceptor() {
        return batchInterceptor;
    }

    public RowInterceptor getRowInterceptor() {
        return rowInterceptor;
    }

    public TableTaskConfig rowInterceptor(RowInterceptor rowInterceptor) {
        this.rowInterceptor = rowInterceptor;
        return this;
    }

    public String getRemoteTable() {
        return remoteTable;
    }

    public void setRemoteTable(String remoteTable) {
        this.remoteTable = remoteTable;
    }

    protected String formatShardTableName(String localTable, int tableIndex) {
        return String.format(localTable, tableIndex);
    }

    public Map<String, String> getColumnNameMapping() {
        return columnNameMapping;
    }

    public void setColumnNameMapping(Map<String, String> columnNameMapping) {
        this.columnNameMapping = columnNameMapping;
    }

    public int getRemoteDbIndex() {
        return remoteDbIndex;
    }

    public void setRemoteDbIndex(int remoteDbIndex) {
        this.remoteDbIndex = remoteDbIndex;
    }


    public String getUpdatedField() {
        return updatedField;
    }

    public void setUpdatedField(String updatedField) {
        this.updatedField = updatedField;
    }


    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public boolean isOnlyFullDump() {
        return onlyFullDump;
    }

    public TableTaskConfig onlyFullDump(boolean onlyFullDump) {
        this.onlyFullDump = onlyFullDump;
        return this;
    }

    public String getSourceExtCondition() {
        return sourceExtCondition;
    }

    public TableTaskConfig sourceExtCondition(String sourceExtCondition) {
        this.sourceExtCondition = sourceExtCondition;
        return this;
    }
}
