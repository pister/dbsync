package com.github.pister.dbsync.config.mapping.table;

import com.github.pister.dbsync.endpoint.server.DbSyncServer;
import com.github.pister.dbsync.config.Column;
import com.github.pister.dbsync.common.db.MagicDb;
import com.github.pister.dbsync.config.Columns;
import com.github.pister.dbsync.config.DbConfig;
import com.github.pister.dbsync.config.TableConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by songlihuang on 2021/1/25.
 */
public class SingleMappedTable extends MappedTable {

    private static final Logger log = LoggerFactory.getLogger(SingleMappedTable.class);

    private int dbIndex = 0;

    @Override
    public boolean isShardSupport() {
        return false;
    }

    @Override
    public void check(DbSyncServer dbSyncServer, MagicDb magicDb, Map<Integer, DbConfig> dbConfigList) throws SQLException {
        log.warn("checking " + getLocalTable() + " ...");
        if (dbConfigList.isEmpty()) {
            throw new RuntimeException("dbConfigList can not be empty!");
        }
        DbConfig dbConfig = dbConfigList.get(dbIndex);
        if (dbConfig == null) {
            throw new RuntimeException("db not exist by index:" + dbIndex);
        }
        TableConfig tableConfig = getTableConfig(dbConfig, getLocalTable());
        Columns localColumns = magicDb.getColumns(tableConfig);
        Columns remoteColumns = dbSyncServer.getColumns(getRemoteDbIndex(), getRemoteTable());

        Map<String, Column> remoteColumnsMap = new HashMap<>();
        for (Column remoteColumn : remoteColumns.getColumns()) {
            remoteColumnsMap.put(remoteColumn.getName(), remoteColumn);
        }

        checkColumns(localColumns, remoteColumnsMap, getLocalTable());
    }

    public int getDbIndex() {
        return dbIndex;
    }

    public void setDbIndex(int dbIndex) {
        this.dbIndex = dbIndex;
    }
}
