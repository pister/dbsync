package com.github.pister.dbsync.config.mapping.table;

import com.github.pister.dbsync.scan.MagicDb;
import com.github.pister.dbsync.TransferServer;
import com.github.pister.dbsync.aop.BatchInterceptor;
import com.github.pister.dbsync.aop.RowInterceptor;
import com.github.pister.dbsync.config.Column;
import com.github.pister.dbsync.config.Columns;
import com.github.pister.dbsync.config.DbConfig;
import com.github.pister.dbsync.config.TableConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Map;

/**
 * Created by songlihuang on 2021/1/25.
 */
public abstract class MappedTable {

    private static final Logger log = LoggerFactory.getLogger(MappedTable.class);

    private int remoteDbIndex;

    private String remoteTable;

    private String localTable;

    private String updatedField = "gmt_modified";

    private boolean onlyFullDump;

    private String sourceExtCondition;

    private Map<String, String> /* local ==> remote */ columnNameMapping;

    private BatchInterceptor batchInterceptor;

    private RowInterceptor rowInterceptor;

    public abstract boolean isShardSupport();

    protected TableConfig getTableConfig(DbConfig dbConfig, String tableName) {
        TableConfig tableConfig = new TableConfig();
        tableConfig.setTableName(tableName);
        tableConfig.setUpdatedField(updatedField);
        tableConfig.setDbConfig(dbConfig);
        return tableConfig;
    }

    protected String getRemoteColumnName(String localColumnName) {
        if (columnNameMapping == null || columnNameMapping.isEmpty()) {
            return localColumnName;
        }
        String remoteName = columnNameMapping.get(localColumnName);
        if (remoteName == null) {
            return localColumnName;
        }
        log.warn("use mapping remote column:" + localColumnName + " ==> " + remoteName);
        return remoteName;
    }

    protected void checkColumns(Columns localColumns, Map<String, Column> remoteColumnsMap, String tableName) {
        for (Column column : localColumns.getColumns()) {
            String expectRemoteColumn = getRemoteColumnName(column.getName());
            Column remoteColumn = remoteColumnsMap.get(expectRemoteColumn);
            if (batchInterceptor == null && rowInterceptor == null) {
                if (remoteColumn == null) {
                    log.warn(tableName + " can not found remote column, local column: " + column.getName());
                    continue;
                }
                if (column.getSqlType() != remoteColumn.getSqlType() && !assignable(column.getSqlType(), remoteColumn.getSqlType())) {
                    throw new RuntimeException(tableName + "'s column type not is same: " + expectRemoteColumn + ", or batchInterceptor/rowInterceptor is not set!");
                }
            }
        }
    }

    private boolean assignable(int toSqlType, int fromSqlType) {
        switch (toSqlType) {
            case Types.BIGINT:
                switch (fromSqlType) {
                    case Types.BIGINT:
                    case Types.INTEGER:
                    case Types.SMALLINT:
                    case Types.TINYINT:
                        return true;
                    default:
                        return false;
                }
            case Types.INTEGER:
                switch (fromSqlType) {
                    case Types.INTEGER:
                    case Types.SMALLINT:
                    case Types.TINYINT:
                        return true;
                    default:
                        return false;
                }
            case Types.SMALLINT:
                switch (fromSqlType) {
                    case Types.SMALLINT:
                    case Types.TINYINT:
                        return true;
                    default:
                        return false;
                }
        }
        return false;
    }

    public abstract void check(TransferServer transferServer, MagicDb magicDb, List<DbConfig> dbConfigList) throws SQLException;


    public int getRemoteDbIndex() {
        return remoteDbIndex;
    }

    public void setRemoteDbIndex(int remoteDbIndex) {
        this.remoteDbIndex = remoteDbIndex;
    }

    public String getRemoteTable() {
        return remoteTable;
    }

    public void setRemoteTable(String remoteTable) {
        this.remoteTable = remoteTable;
    }

    public String getLocalTable() {
        return localTable;
    }

    public void setLocalTable(String localTable) {
        this.localTable = localTable;
    }

    public String getUpdatedField() {
        return updatedField;
    }

    public void setUpdatedField(String updatedField) {
        this.updatedField = updatedField;
    }

    public Map<String, String> getColumnNameMapping() {
        return columnNameMapping;
    }

    public void setColumnNameMapping(Map<String, String> columnNameMapping) {
        this.columnNameMapping = columnNameMapping;
    }

    public BatchInterceptor getBatchInterceptor() {
        return batchInterceptor;
    }

    public void setBatchInterceptor(BatchInterceptor batchInterceptor) {
        this.batchInterceptor = batchInterceptor;
    }

    public boolean isOnlyFullDump() {
        return onlyFullDump;
    }

    public void setOnlyFullDump(boolean onlyFullDump) {
        this.onlyFullDump = onlyFullDump;
    }

    public String getSourceExtCondition() {
        return sourceExtCondition;
    }

    public void setSourceExtCondition(String sourceExtCondition) {
        this.sourceExtCondition = sourceExtCondition;
    }

    public RowInterceptor getRowInterceptor() {
        return rowInterceptor;
    }

    public void setRowInterceptor(RowInterceptor rowInterceptor) {
        this.rowInterceptor = rowInterceptor;
    }
}
