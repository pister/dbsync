package com.github.pister.dbsync;

import com.github.pister.dbsync.db.DbPool;
import com.github.pister.dbsync.row.RowProcessor;
import com.github.pister.dbsync.scan.MagicDb;
import com.github.pister.dbsync.scan.Scanner;
import com.github.pister.dbsync.util.CollectionUtil;
import com.github.pister.dbsync.base.AbstractPoint;
import com.github.pister.dbsync.base.DbTable;
import com.github.pister.dbsync.config.Columns;
import com.github.pister.dbsync.config.DbConfig;
import com.github.pister.dbsync.config.TableConfig;
import com.github.pister.dbsync.rt.Pagination;
import com.github.pister.dbsync.rt.ScanPageResult;
import com.github.pister.dbsync.sync.DbPoolQueryProcessor;
import com.github.pister.dbsync.sync.QueryProcessor;
import com.github.pister.dbsync.util.MapUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Created by songlihuang on 2017/7/11.
 */
public class DefaultTransferServer extends AbstractPoint implements TransferServer {

    private static final Logger log = LoggerFactory.getLogger(DefaultTransferServer.class);

    private List<DbConfig> dbConfigList = CollectionUtil.newArrayList();

    private DbPool dbPool;

    private MagicDb magicDb;

    private Scanner scanner;

    private Map<Integer, List<TableConfig>> /*dbIndex ==>List<TableConfig> */ tableConfigListMap;

    private Map<DbTable, TableConfig> namedTableConfig;

    public void init() throws SQLException {
        dbPool = new DbPool();
        magicDb = new MagicDb(dbPool);
        scanner = new Scanner(magicDb);
        tableConfigListMap = MapUtil.newHashMap();
        namedTableConfig = MapUtil.newHashMap();


        for (int i = 0, len = dbConfigList.size(); i < len; i++) {
            DbConfig dbConfig = dbConfigList.get(i);
            List<String> tableNames = magicDb.listTables(dbConfig);
            List<TableConfig> tableConfigList = CollectionUtil.newArrayList(tableNames.size());
            for (String tableName : tableNames) {
                TableConfig tableConfig = createTableConfig(tableName, i, dbConfig);
                tableConfigList.add(tableConfig);
                namedTableConfig.put(new DbTable(i, tableName), tableConfig);
            }
            tableConfigListMap.put(i, tableConfigList);
        }
        log.warn("DefaultTransferServer init success.");
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
        return CollectionUtil.transformList(dbConfigList, object -> object.getDbName());
    }

    public List<String> tableNameList(final int dbIndex) {
        List<TableConfig> tableConfigList = tableConfigListMap.get(dbIndex);
        if (CollectionUtil.isEmpty(tableConfigList)) {
            return CollectionUtil.newArrayList();
        }
        return CollectionUtil.transformList(tableConfigList, object -> object.getTableName());
    }

    @Override
    public <T> List<T> executeForList(int dbIndex, RowProcessor<T> rowProcessor, String sql, Object[] params) throws SQLException {
        DbConfig dbConfig = dbConfigList.get(dbIndex);
        return magicDb.getDbPool().executeForList(dbConfig, rowProcessor, sql, params);
    }

    @Override
    public <T> T executeForObject(int dbIndex, RowProcessor<T> rowProcessor, String sql, Object[] params) throws SQLException {
        DbConfig dbConfig = dbConfigList.get(dbIndex);
        return magicDb.getDbPool().executeForObject(dbConfig, rowProcessor, sql, params);
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

    @Override
    public QueryProcessor createQueryProcessor() {
        return new DbPoolQueryProcessor(dbPool, dbConfigList);
    }

    /**
     * 请注意顺序，使用的过程要按0,1,2,3...序列获取
     *
     * @param dbConfig
     */
    public void addDbConfig(DbConfig dbConfig) {
        dbConfigList.add(dbConfig);
    }


}
