package com.github.pister.dbsync.config.mapping.table;

import com.github.pister.dbsync.endpoint.server.DbSyncServer;
import com.github.pister.dbsync.common.db.shard.ShardStrategy;
import com.github.pister.dbsync.config.Column;
import com.github.pister.dbsync.runtime.exec.Row;
import com.github.pister.dbsync.common.db.MagicDb;
import com.github.pister.dbsync.config.Columns;
import com.github.pister.dbsync.config.DbConfig;
import com.github.pister.dbsync.config.TableConfig;
import com.github.pister.dbsync.runtime.exec.FieldValue;
import com.github.pister.dbsync.common.db.shard.ShardInfo;
import com.github.pister.dbsync.common.tools.util.MapUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Created by songlihuang on 2021/1/25.
 */
public class ShardMappedTable extends MappedTable {

    private static final Logger log = LoggerFactory.getLogger(ShardMappedTable.class);

    private ShardStrategy shardStrategy;

    private String routeColumn;

    private int dbIndexOffset = 0;

    private int dbCount = 1;

    private int tableCountPerDb = 1;

    public ShardMappedTable(String routeColumn, int dbCount, int tableCountPerDb, int dbIndexOffset, ShardStrategy shardStrategy) {
        this.routeColumn = routeColumn;
        this.dbCount = dbCount;
        this.tableCountPerDb = tableCountPerDb;
        this.dbIndexOffset = dbIndexOffset;
        this.shardStrategy = shardStrategy;
    }

    @Override
    public boolean isShardSupport() {
        return true;
    }

    public String formatTableName(int dbIndex, int tableIndex) {
        return String.format(getLocalTable(), tableIndex);
    }

    @Override
    public void check(DbSyncServer dbSyncServer, MagicDb magicDb, Map<Integer, DbConfig> localDbConfigs) throws SQLException {
        log.warn("checking " + getLocalTable() + " ...");
        final Columns remoteColumns = dbSyncServer.getColumns(getRemoteDbIndex(), getRemoteTable());
        if (remoteColumns == null) {
            throw new RuntimeException("can not found table: " + getRemoteTable() + " on the source db index: " + getRemoteDbIndex());
        }

        Map<String, Column> remoteColumnsMap = MapUtil.newHashMap();
        for (Column remoteColumn : remoteColumns.getColumns()) {
            remoteColumnsMap.put(remoteColumn.getName(), remoteColumn);
        }

        int tableIndex = 0;
        for (int dbIndex = dbIndexOffset; dbIndex < dbCount + dbIndexOffset; dbIndex++) {
            DbConfig dbConfig = localDbConfigs.get(dbIndex);
            if (dbConfig == null) {
                throw new RuntimeException("local db not exist by index:" + dbIndex);
            }
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
        final long longValue = shardStrategy.routing(routeValue);
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
