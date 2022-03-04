package com.jiudengnile.common.transfer.config.mapping.table;

import com.jiudengnile.common.transfer.TransferServer;
import com.jiudengnile.common.transfer.config.Column;
import com.jiudengnile.common.transfer.config.Columns;
import com.jiudengnile.common.transfer.config.DbConfig;
import com.jiudengnile.common.transfer.config.TableConfig;
import com.jiudengnile.common.transfer.scan.MagicDb;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wint.lang.utils.MapUtil;

import java.sql.SQLException;
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
    public void check(TransferServer transferServer, MagicDb magicDb, List<DbConfig> dbConfigList) throws SQLException {
        log.warn("checking " + getLocalTable() + " ...");
        if (dbConfigList.isEmpty()) {
            throw new RuntimeException("dbConfigList can not be empty!");
        }
        DbConfig dbConfig = dbConfigList.get(dbIndex);
        TableConfig tableConfig = getTableConfig(dbConfig, getLocalTable());
        Columns localColumns = magicDb.getColumns(tableConfig);
        Columns remoteColumns = transferServer.getColumns(getRemoteDbIndex(), getRemoteTable());

        Map<String, Column> remoteColumnsMap = MapUtil.newHashMap();
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
