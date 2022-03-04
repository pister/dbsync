package com.jiudengnile.common.transfer.sync;

import com.jiudengnile.common.transfer.config.DbConfig;
import com.jiudengnile.common.transfer.config.mapping.table.MappedTable;
import com.jiudengnile.common.transfer.config.mapping.table.ShardMappedTable;
import com.jiudengnile.common.transfer.config.mapping.table.SingleMappedTable;
import com.jiudengnile.common.transfer.db.DbPool;
import com.jiudengnile.common.transfer.shard.ShardInfo;
import wint.help.sql.RowProcessor;

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
