package com.github.pister.dbsync.sync;

import com.github.pister.dbsync.config.mapping.table.MappedTable;
import com.github.pister.dbsync.config.mapping.table.ShardMappedTable;
import com.github.pister.dbsync.config.mapping.table.SingleMappedTable;
import com.github.pister.dbsync.db.DbPool;
import com.github.pister.dbsync.row.RowProcessor;
import com.github.pister.dbsync.shard.ShardInfo;
import com.github.pister.dbsync.config.DbConfig;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by songlihuang on 2021/3/12.
 */
public class DbPoolQueryProcessor implements QueryProcessor {

    private DbPool dbPool;

    private List<DbConfig> dbConfigList;

    public DbPoolQueryProcessor(DbPool dbPool, List<DbConfig> dbConfigList) {
        this.dbPool = dbPool;
        this.dbConfigList = dbConfigList;
    }

    @Override
    public <T> T queryForObject(MappedTable mappedTable, RowProcessor<T> rowProcessor, String sql, Object routeValue, Object... args) throws SQLException {
        if (mappedTable.isShardSupport()) {
            ShardMappedTable shardMappedTable = (ShardMappedTable) mappedTable;
            ShardInfo shardInfo = shardMappedTable.route(routeValue);
            int dbOffset = shardMappedTable.getDbIndexOffset();
            DbConfig dbConfig = dbConfigList.get(shardInfo.getDatabaseIndex() + dbOffset);
            String tableName = shardMappedTable.formatTableName(shardInfo.getDatabaseIndex(), shardInfo.getTableIndex());
            String realSql = sql.replace("$table$", tableName);
            return dbPool.executeForObject(dbConfig, rowProcessor, realSql, args);
        } else {
            SingleMappedTable singleMappedTable = (SingleMappedTable) mappedTable;
            DbConfig dbConfig = dbConfigList.get(singleMappedTable.getDbIndex());
            return dbPool.executeForObject(dbConfig, rowProcessor, sql, args);
        }
    }

    @Override
    public <T> List<T> queryForList(MappedTable mappedTable, RowProcessor<T> rowProcessor, String sql, Object routeValue, Object... args) throws SQLException {
        if (mappedTable.isShardSupport()) {
            ShardMappedTable shardMappedTable = (ShardMappedTable) mappedTable;
            ShardInfo shardInfo = shardMappedTable.route(routeValue);
            int dbOffset = shardMappedTable.getDbIndexOffset();
            DbConfig dbConfig = dbConfigList.get(shardInfo.getDatabaseIndex() + dbOffset);
            String tableName = shardMappedTable.formatTableName(shardInfo.getDatabaseIndex(), shardInfo.getTableIndex());
            String realSql = sql.replace("$table$", tableName);
            return dbPool.executeForList(dbConfig, rowProcessor, realSql, args);
        } else {
            SingleMappedTable singleMappedTable = (SingleMappedTable) mappedTable;
            DbConfig dbConfig = dbConfigList.get(singleMappedTable.getDbIndex());
            return dbPool.executeForList(dbConfig, rowProcessor, sql, args);
        }
    }

}
