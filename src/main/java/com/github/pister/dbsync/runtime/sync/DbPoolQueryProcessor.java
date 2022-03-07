package com.github.pister.dbsync.runtime.sync;

import com.github.pister.dbsync.config.mapping.table.MappedTable;
import com.github.pister.dbsync.config.mapping.table.ShardMappedTable;
import com.github.pister.dbsync.config.mapping.table.SingleMappedTable;
import com.github.pister.dbsync.common.db.DbPool;
import com.github.pister.dbsync.runtime.exec.RowProcessor;
import com.github.pister.dbsync.common.db.shard.ShardInfo;
import com.github.pister.dbsync.config.DbConfig;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Created by songlihuang on 2021/3/12.
 */
public class DbPoolQueryProcessor implements QueryProcessor {

    private DbPool dbPool;

    private Map<Integer, DbConfig> dbConfigMap;

    public DbPoolQueryProcessor(DbPool dbPool, Map<Integer, DbConfig> dbConfigMap) {
        this.dbPool = dbPool;
        this.dbConfigMap = dbConfigMap;
    }

    @Override
    public <T> T queryForObject(MappedTable mappedTable, RowProcessor<T> rowProcessor, String sql, Object routeValue, Object... args) throws SQLException {
        if (mappedTable.isShardSupport()) {
            ExecuteContext<T> executeContext = new ExecuteContext<T>();
            executeContext.prepare(mappedTable, routeValue, sql);
            return executeContext.executeForObject(rowProcessor, args);
        } else {
            SingleMappedTable singleMappedTable = (SingleMappedTable) mappedTable;
            DbConfig dbConfig = dbConfigMap.get(singleMappedTable.getDbIndex());
            return dbPool.executeForObject(dbConfig, rowProcessor, sql, args);
        }
    }

    @Override
    public <T> List<T> queryForList(MappedTable mappedTable, RowProcessor<T> rowProcessor, String sql, Object routeValue, Object... args) throws SQLException {
        if (mappedTable.isShardSupport()) {
            ExecuteContext<T> executeContext = new ExecuteContext<T>();
            executeContext.prepare(mappedTable, routeValue, sql);
            return executeContext.executeForList(rowProcessor, args);
        } else {
            SingleMappedTable singleMappedTable = (SingleMappedTable) mappedTable;
            DbConfig dbConfig = dbConfigMap.get(singleMappedTable.getDbIndex());
            return dbPool.executeForList(dbConfig, rowProcessor, sql, args);
        }
    }

    private class ExecuteContext<T> {
        private DbConfig dbConfig;
        private String realSql;

        public void prepare(MappedTable mappedTable, Object routeValue, String sql) {
            ShardMappedTable shardMappedTable = (ShardMappedTable) mappedTable;
            ShardInfo shardInfo = shardMappedTable.route(routeValue);
            int dbOffset = shardMappedTable.getDbIndexOffset();
            this.dbConfig = dbConfigMap.get(shardInfo.getDatabaseIndex() + dbOffset);
            String tableName = shardMappedTable.formatTableName(shardInfo.getDatabaseIndex(), shardInfo.getTableIndex());
            this.realSql = sql.replace("$table$", tableName);
        }

        public T executeForObject(RowProcessor<T> rowProcessor, Object[] args) throws SQLException {
            return dbPool.executeForObject(dbConfig, rowProcessor, realSql, args);
        }

        public List<T> executeForList(RowProcessor<T> rowProcessor, Object[] args) throws SQLException {
            return dbPool.executeForList(dbConfig, rowProcessor, realSql, args);
        }
    }

}
