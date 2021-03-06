package com.github.pister.dbsync.endpoint.server;

import com.github.pister.dbsync.common.db.DbPool;
import com.github.pister.dbsync.common.db.MagicDb;
import com.github.pister.dbsync.common.tools.util.MySqlUtil;
import com.github.pister.dbsync.runtime.scan.Scanner;
import com.github.pister.dbsync.common.tools.util.CollectionUtil;
import com.github.pister.dbsync.endpoint.base.AbstractPoint;
import com.github.pister.dbsync.endpoint.base.DbTable;
import com.github.pister.dbsync.config.Columns;
import com.github.pister.dbsync.config.DbConfig;
import com.github.pister.dbsync.config.TableConfig;
import com.github.pister.dbsync.runtime.exec.Pagination;
import com.github.pister.dbsync.runtime.exec.ScanPageResult;
import com.github.pister.dbsync.runtime.sync.DbPoolQueryProcessor;
import com.github.pister.dbsync.runtime.sync.QueryProcessor;
import com.github.pister.dbsync.common.tools.util.MapUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Created by songlihuang on 2017/7/11.
 */
public class DefaultSyncServer extends AbstractPoint implements SyncServer {

    private static final Logger log = LoggerFactory.getLogger(DefaultSyncServer.class);

    private Map<Integer, DbConfig> dbConfigMap = MapUtil.newHashMap();

    private DbPool dbPool;

    private MagicDb magicDb;

    private Scanner scanner;

    private Map<Integer, List<TableConfig>> /*dbIndex ==>List<TableConfig> */ tableConfigListMap;

    private Map<DbTable, TableConfig> namedTableConfig;

    public void init() throws SQLException {
        if (!hasInited.compareAndSet(false, true)) {
            throw new RuntimeException("has already inited");
        }
        dbPool = new DbPool();
        magicDb = new MagicDb(dbPool);
        scanner = new Scanner(magicDb);
        tableConfigListMap = MapUtil.newHashMap();
        namedTableConfig = MapUtil.newHashMap();

        for (Map.Entry<Integer, DbConfig> entry : dbConfigMap.entrySet()) {
            int index = entry.getKey();
            DbConfig dbConfig = entry.getValue();
            List<String> tableNames = magicDb.listTables(dbConfig);
            List<TableConfig> tableConfigList = CollectionUtil.newArrayList(tableNames.size());
            for (String tableName : tableNames) {
                TableConfig tableConfig = createTableConfig(tableName, index, dbConfig);
                tableConfigList.add(tableConfig);
                namedTableConfig.put(new DbTable(index, tableName), tableConfig);
            }
            tableConfigListMap.put(index, tableConfigList);
        }

        log.warn("DefaultSyncServer init success.");
    }


    @Override
    public ScanPageResult fetchForPage(int dbIndex, String tableName, Pagination pagination, String extSqlCondition) {
        try {
            TableConfig tableConfig = namedTableConfig.get(new DbTable(dbIndex, tableName));
            if (tableConfig == null) {
                ScanPageResult scanPageResult = new ScanPageResult();
                scanPageResult.setSuccess(false);
                scanPageResult.setMessage("table not exist, db: " + dbIndex + ", table: " + tableName);
                return scanPageResult;
            }
            ScanPageResult scanPageResult = scanner.fetchForPage(pagination, tableConfig, extSqlCondition);
            scanPageResult.setSuccess(true);
            return scanPageResult;
        } catch (Exception e) {
            log.error("error", e);
            ScanPageResult scanPageResult = new ScanPageResult();
            scanPageResult.setSuccess(false);
            scanPageResult.setMessage("system error:" + e.getMessage());
            return scanPageResult;
        }
    }

    public List<String> dbNameList() {
        return CollectionUtil.transformList(dbConfigMap.values(), object -> object.getDbName());
    }

    public List<String> tableNameList(final int dbIndex) {
        List<TableConfig> tableConfigList = tableConfigListMap.get(dbIndex);
        if (CollectionUtil.isEmpty(tableConfigList)) {
            return CollectionUtil.newArrayList();
        }
        return CollectionUtil.transformList(tableConfigList, object -> object.getTableName());
    }


    @Override
    public Columns getColumns(int dbIndex, String tableName) {
        TableConfig tableConfig = namedTableConfig.get(new DbTable(dbIndex, tableName));
        if (tableConfig == null) {
            return null;
        }
        try {
            return magicDb.getColumns(tableConfig);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * ??????????????????????????????????????????
     * ???????????????????????????????????????
     * ??????????????????????????????????????????
     *
     * @param dbIndex
     * @param dbConfig
     */
    public void registerDbConfig(int dbIndex, DbConfig dbConfig) {
        if (hasInited.get()) {
            throw new IllegalStateException("can not register after inited");
        }
        dbConfigMap.put(dbIndex, dbConfig);
    }

    /**
     * ??????????????????????????????????????????
     * ???????????????????????????????????????
     * ??????????????????????????????????????????
     * @param dbIndex
     * @param shortUrl hostname[:port]/db_name
     * @param username
     * @param password
     */
    public void registerDbConfig(int dbIndex, String shortUrl, String username, String password) {
        registerDbConfig(dbIndex, MySqlUtil.makeDbConfig(shortUrl, username, password));
    }

}
