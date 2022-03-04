package com.jiudengnile.common.transfer.config.mapping.table;

import com.jiudengnile.common.transfer.TransferServer;
import com.jiudengnile.common.transfer.config.Column;
import com.jiudengnile.common.transfer.config.Columns;
import com.jiudengnile.common.transfer.config.DbConfig;
import com.jiudengnile.common.transfer.config.TableConfig;
import com.jiudengnile.common.transfer.rt.FieldValue;
import com.jiudengnile.common.transfer.rt.Row;
import com.jiudengnile.common.transfer.scan.MagicDb;
import com.jiudengnile.common.transfer.shard.RouteUtil;
import com.jiudengnile.common.transfer.shard.ShardInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wint.lang.utils.MapUtil;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Created by songlihuang on 2021/1/25.
 */
public class ShardMappedTable extends MappedTable {

    private static final Logger log = LoggerFactory.getLogger(ShardMappedTable.class);

    private String routeColumn;

    private int dbIndexOffset = 0;

    private int dbCount = 1;

    private int tableCountPerDb = 1;

    public ShardMappedTable(String routeColumn, int dbCount, int tableCountPerDb, int dbIndexOffset) {
        this.routeColumn = routeColumn;
        this.dbCount = dbCount;
        this.tableCountPerDb = tableCountPerDb;
        this.dbIndexOffset = dbIndexOffset;
    }

    @Override
    public boolean isShardSupport() {
        return true;
    }

    public String formatTableName(int dbIndex, int tableIndex) {
        return String.format(getLocalTable(), tableIndex);
    }

    @Override
    public void check(TransferServer transferServer, MagicDb magicDb, List<DbConfig> localDbConfigList) throws SQLException {
        log.warn("checking " + getLocalTable() + " ...");
        if (dbCount + dbIndexOffset > localDbConfigList.size()) {
            throw new RuntimeException("db and  not match, need: " + dbCount + " and index offset:" + dbIndexOffset + " but count:" + localDbConfigList.size());
        }
        final Columns remoteColumns = transferServer.getColumns(getRemoteDbIndex(), getRemoteTable());
        if (remoteColumns == null) {
            throw new RuntimeException("can not found table: " + getRemoteTable() + " on the source db index: " + getRemoteDbIndex());
        }

        Map<String, Column> remoteColumnsMap = MapUtil.newHashMap();
        for (Column remoteColumn : remoteColumns.getColumns()) {
            remoteColumnsMap.put(remoteColumn.getName(), remoteColumn);
        }

        int tableIndex = 0;
        for (int dbIndex = dbIndexOffset; dbIndex < dbCount + dbIndexOffset; dbIndex++) {
            DbConfig dbConfig = localDbConfigList.get(dbIndex);
            for (int i = 0; i < tableCountPerDb; i++) {
                String tableName = formatTableName(dbIndex, tableIndex);
                log.warn("checking shard table: " + tableName + " ...");
                TableConfig tableConfig = getTableConfig(dbConfig, tableName);
                Columns localColumns = magicDb.getColumns(tableConfig);
                checkColumns(localColumns, remoteColumnsMap, tableName);
                tableIndex++;
            }
        }
    }

    public ShardInfo route(Row row) {
        FieldValue fieldValue = row.getFields().get(routeColumn);
        if (fieldValue == null) {
            throw new IllegalArgumentException("find can not find route column:" + routeColumn);
        }
        return route(fieldValue.getValue());
    }

    public ShardInfo route(Object routeValue) {
        final long longValue = RouteUtil.getLongValue(routeValue);
        int totalTableCount = dbCount * tableCountPerDb;
        if (totalTableCount == 0) {
            throw new RuntimeException("totalTableCount can not be zero!");
        }
        int tableIndex = (int) (longValue % totalTableCount);
        int dbIndex = tableIndex / tableCountPerDb;
        ShardInfo shardInfo = new ShardInfo();
        shardInfo.setDatabaseIndex(dbIndex);
        shardInfo.setTableIndex(tableIndex);
        return shardInfo;
    }

    public int getDbIndexOffset() {
        return dbIndexOffset;
    }

    public void setDbIndexOffset(int dbIndexOffset) {
        this.dbIndexOffset = dbIndexOffset;
    }
}
